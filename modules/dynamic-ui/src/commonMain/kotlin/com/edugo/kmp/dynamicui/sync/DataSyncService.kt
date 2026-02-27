package com.edugo.kmp.dynamicui.sync

import com.edugo.kmp.auth.model.MenuItem
import com.edugo.kmp.auth.model.MenuResponse
import com.edugo.kmp.auth.model.UserContext
import com.edugo.kmp.dynamicui.loader.CachedScreenLoader
import com.edugo.kmp.dynamicui.model.DataConfig
import com.edugo.kmp.dynamicui.model.ScreenDefinition
import com.edugo.kmp.dynamicui.model.ScreenPattern
import com.edugo.kmp.dynamicui.model.ScreenTemplate
import com.edugo.kmp.dynamicui.sync.model.BucketData
import com.edugo.kmp.dynamicui.sync.model.ScreenBundleEntry
import com.edugo.kmp.dynamicui.sync.model.SyncBundleResponse
import com.edugo.kmp.dynamicui.sync.model.SyncState
import com.edugo.kmp.dynamicui.sync.model.UserDataBundle
import com.edugo.kmp.foundation.result.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement

class DataSyncService(
    private val repository: SyncRepository,
    private val store: LocalSyncStore,
    private val cachedScreenLoader: CachedScreenLoader,
    private val json: Json = Json { ignoreUnknownKeys = true; isLenient = true },
) {
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _currentBundle = MutableStateFlow<UserDataBundle?>(null)
    val currentBundle: StateFlow<UserDataBundle?> = _currentBundle.asStateFlow()

    private val syncMutex = Mutex()

    suspend fun fullSync(): Result<UserDataBundle> = syncMutex.withLock {
        _syncState.value = SyncState.Syncing

        return when (val result = repository.getBundle()) {
            is Result.Success -> {
                val bundle = mapBundleResponse(result.data)
                store.saveBundle(bundle)
                seedScreenLoader(bundle.screens)
                _currentBundle.value = bundle
                _syncState.value = SyncState.Synced(bundle.syncedAt)
                Result.Success(bundle)
            }
            is Result.Failure -> {
                _syncState.value = SyncState.Error(result.error)
                val local = store.loadBundle()
                if (local != null) {
                    _currentBundle.value = local
                    _syncState.value = SyncState.Stale(local.syncedAt)
                }
                Result.Failure(result.error)
            }
            is Result.Loading -> Result.Failure("Unexpected loading state")
        }
    }

    suspend fun deltaSync(): Result<UserDataBundle> = syncMutex.withLock {
        val localHashes = store.getHashes()
        if (localHashes.isEmpty()) {
            return performFullSyncUnlocked()
        }

        _syncState.value = SyncState.Syncing

        return when (val result = repository.deltaSync(localHashes)) {
            is Result.Success -> {
                val delta = result.data
                val now = Clock.System.now()

                // Build updated bundle incrementally from current in-memory bundle
                val baseBundle = _currentBundle.value ?: store.loadBundle()
                if (baseBundle != null) {
                    val updatedBundle = applyDeltaToBundle(baseBundle, delta.changed, now)
                    store.updateSyncedAt(now)
                    _currentBundle.value = updatedBundle
                    _syncState.value = SyncState.Synced(now)

                    // Seed screen loader with changed screens only
                    val changedScreens = delta.changed.keys
                        .filter { it.startsWith("screen:") }
                        .associate { key ->
                            val screenKey = key.removePrefix("screen:")
                            screenKey to updatedBundle.screens[screenKey]
                        }
                        .filterValues { it != null }
                        .mapValues { it.value!! }
                    if (changedScreens.isNotEmpty()) {
                        seedScreenLoader(changedScreens)
                    }

                    Result.Success(updatedBundle)
                } else {
                    // No base bundle available, fall back to full sync
                    _syncState.value = SyncState.Error("No base bundle for delta")
                    performFullSyncUnlocked()
                }
            }
            is Result.Failure -> {
                _syncState.value = SyncState.Error(result.error)
                val local = store.loadBundle()
                if (local != null) {
                    _currentBundle.value = local
                    _syncState.value = SyncState.Stale(local.syncedAt)
                }
                Result.Failure(result.error)
            }
            is Result.Loading -> Result.Failure("Unexpected loading state")
        }
    }

    suspend fun restoreFromLocal(): UserDataBundle? {
        val bundle = store.loadBundle()
        if (bundle != null) {
            _currentBundle.value = bundle
            seedScreenLoader(bundle.screens)
            _syncState.value = SyncState.Stale(bundle.syncedAt)
        }
        return bundle
    }

    fun clearAll() {
        store.clearAll()
        _currentBundle.value = null
        _syncState.value = SyncState.Idle
    }

    private suspend fun performFullSyncUnlocked(): Result<UserDataBundle> {
        _syncState.value = SyncState.Syncing

        return when (val result = repository.getBundle()) {
            is Result.Success -> {
                val bundle = mapBundleResponse(result.data)
                store.saveBundle(bundle)
                seedScreenLoader(bundle.screens)
                _currentBundle.value = bundle
                _syncState.value = SyncState.Synced(bundle.syncedAt)
                Result.Success(bundle)
            }
            is Result.Failure -> {
                _syncState.value = SyncState.Error(result.error)
                Result.Failure(result.error)
            }
            is Result.Loading -> Result.Failure("Unexpected loading state")
        }
    }

    private fun mapBundleResponse(response: SyncBundleResponse): UserDataBundle {
        val now = Clock.System.now()
        val screens = response.screens.mapValues { (_, entry) -> mapScreenEntry(entry) }

        return UserDataBundle(
            menu = MenuResponse(items = response.menu),
            permissions = response.permissions,
            screens = screens,
            availableContexts = response.availableContexts,
            hashes = response.hashes,
            syncedAt = now,
        )
    }

    private fun mapScreenEntry(entry: ScreenBundleEntry): ScreenDefinition {
        val pattern = try {
            json.decodeFromString<ScreenPattern>("\"${entry.pattern}\"")
        } catch (_: Exception) {
            ScreenPattern.LIST
        }

        val template = if (entry.template != null) {
            try {
                json.decodeFromJsonElement<ScreenTemplate>(entry.template)
            } catch (_: Exception) {
                ScreenTemplate(zones = emptyList())
            }
        } else {
            ScreenTemplate(zones = emptyList())
        }

        val dataConfig = if (entry.dataConfig != null) {
            try {
                json.decodeFromJsonElement<DataConfig>(entry.dataConfig)
            } catch (_: Exception) {
                null
            }
        } else null

        return ScreenDefinition(
            screenKey = entry.screenKey,
            screenName = entry.screenName,
            pattern = pattern,
            version = entry.version,
            template = template,
            slotData = entry.slotData,
            dataConfig = dataConfig,
            handlerKey = entry.handlerKey,
            updatedAt = entry.updatedAt,
        )
    }

    private fun applyDeltaToBundle(
        base: UserDataBundle,
        changed: Map<String, BucketData>,
        syncedAt: Instant,
    ): UserDataBundle {
        var menu = base.menu
        var permissions = base.permissions
        var contexts = base.availableContexts
        val screens = base.screens.toMutableMap()
        val hashes = base.hashes.toMutableMap()

        for ((key, bucketData) in changed) {
            when {
                key == "menu" -> {
                    try {
                        val items = json.decodeFromJsonElement<List<MenuItem>>(bucketData.data)
                        val newMenu = MenuResponse(items = items)
                        menu = newMenu
                        hashes[key] = bucketData.hash
                        store.updateMenu(newMenu, bucketData.hash)
                    } catch (_: Exception) { }
                }
                key == "permissions" -> {
                    try {
                        val perms = json.decodeFromJsonElement<List<String>>(bucketData.data)
                        permissions = perms
                        hashes[key] = bucketData.hash
                        store.updatePermissions(perms, bucketData.hash)
                    } catch (_: Exception) { }
                }
                key == "available_contexts" -> {
                    try {
                        val ctxs = json.decodeFromJsonElement<List<UserContext>>(bucketData.data)
                        contexts = ctxs
                        hashes[key] = bucketData.hash
                        store.updateContexts(ctxs, bucketData.hash)
                    } catch (_: Exception) { }
                }
                key.startsWith("screen:") -> {
                    val screenKey = key.removePrefix("screen:")
                    try {
                        val entry = json.decodeFromJsonElement<ScreenBundleEntry>(bucketData.data)
                        val screen = mapScreenEntry(entry)
                        screens[screenKey] = screen
                        hashes[key] = bucketData.hash
                        store.updateScreen(screenKey, screen, bucketData.hash)
                    } catch (_: Exception) { }
                }
            }
        }

        return UserDataBundle(
            menu = menu,
            permissions = permissions,
            screens = screens,
            availableContexts = contexts,
            hashes = hashes,
            syncedAt = syncedAt,
        )
    }

    private suspend fun seedScreenLoader(screens: Map<String, ScreenDefinition>) {
        cachedScreenLoader.seedFromBundle(screens)
    }
}

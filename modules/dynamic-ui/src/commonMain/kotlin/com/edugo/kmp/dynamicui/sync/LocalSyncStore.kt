package com.edugo.kmp.dynamicui.sync

import com.edugo.kmp.auth.model.MenuResponse
import com.edugo.kmp.auth.model.UserContext
import com.edugo.kmp.dynamicui.model.ScreenDefinition
import com.edugo.kmp.dynamicui.sync.model.UserDataBundle
import com.edugo.kmp.storage.SafeEduGoStorage
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class LocalSyncStore(
    private val storage: SafeEduGoStorage,
    private val json: Json = Json { ignoreUnknownKeys = true },
) {
    companion object {
        private const val KEY_HASHES = "sync.hashes"
        private const val KEY_MENU = "sync.menu"
        private const val KEY_PERMISSIONS = "sync.permissions"
        private const val KEY_CONTEXTS = "sync.contexts"
        private const val KEY_SYNCED_AT = "sync.synced_at"
        private const val SCREEN_PREFIX = "sync.screen."
        private const val KEY_SCREEN_KEYS = "sync.screen_keys"
    }

    fun saveBundle(bundle: UserDataBundle) {
        storage.putStringSafe(KEY_MENU, json.encodeToString(bundle.menu))
        storage.putStringSafe(KEY_PERMISSIONS, json.encodeToString(bundle.permissions))
        storage.putStringSafe(KEY_CONTEXTS, json.encodeToString(bundle.availableContexts))
        storage.putStringSafe(KEY_HASHES, json.encodeToString(bundle.hashes))
        storage.putStringSafe(KEY_SYNCED_AT, bundle.syncedAt.toEpochMilliseconds().toString())

        val screenKeys = mutableListOf<String>()
        for ((key, screen) in bundle.screens) {
            storage.putStringSafe("$SCREEN_PREFIX$key", json.encodeToString(screen))
            screenKeys.add(key)
        }
        storage.putStringSafe(KEY_SCREEN_KEYS, json.encodeToString(screenKeys))
    }

    fun loadBundle(): UserDataBundle? {
        val menuJson = storage.getStringSafe(KEY_MENU)
        val permJson = storage.getStringSafe(KEY_PERMISSIONS)
        val ctxJson = storage.getStringSafe(KEY_CONTEXTS)
        val hashesJson = storage.getStringSafe(KEY_HASHES)
        val syncedAtStr = storage.getStringSafe(KEY_SYNCED_AT)

        if (menuJson.isBlank() || hashesJson.isBlank() || syncedAtStr.isBlank()) return null

        return try {
            val menu = json.decodeFromString<MenuResponse>(menuJson)
            val permissions = if (permJson.isNotBlank()) {
                json.decodeFromString<List<String>>(permJson)
            } else emptyList()
            val contexts = if (ctxJson.isNotBlank()) {
                json.decodeFromString<List<UserContext>>(ctxJson)
            } else emptyList()
            val hashes = json.decodeFromString<Map<String, String>>(hashesJson)
            val syncedAt = Instant.fromEpochMilliseconds(syncedAtStr.toLong())

            val screens = loadAllScreens()

            UserDataBundle(
                menu = menu,
                permissions = permissions,
                screens = screens,
                availableContexts = contexts,
                hashes = hashes,
                syncedAt = syncedAt,
            )
        } catch (_: Exception) {
            null
        }
    }

    fun getHashes(): Map<String, String> {
        val hashesJson = storage.getStringSafe(KEY_HASHES)
        if (hashesJson.isBlank()) return emptyMap()
        return try {
            json.decodeFromString(hashesJson)
        } catch (_: Exception) {
            emptyMap()
        }
    }

    fun updateMenu(menu: MenuResponse, hash: String) {
        storage.putStringSafe(KEY_MENU, json.encodeToString(menu))
        updateHash("menu", hash)
    }

    fun updatePermissions(permissions: List<String>, hash: String) {
        storage.putStringSafe(KEY_PERMISSIONS, json.encodeToString(permissions))
        updateHash("permissions", hash)
    }

    fun updateScreen(key: String, screen: ScreenDefinition, hash: String) {
        storage.putStringSafe("$SCREEN_PREFIX$key", json.encodeToString(screen))
        updateHash("screen:$key", hash)
        addScreenKey(key)
    }

    fun removeScreen(key: String) {
        storage.removeSafe("$SCREEN_PREFIX$key")
        removeHash("screen:$key")
        removeScreenKey(key)
    }

    fun updateContexts(contexts: List<UserContext>, hash: String) {
        storage.putStringSafe(KEY_CONTEXTS, json.encodeToString(contexts))
        updateHash("available_contexts", hash)
    }

    fun updateSyncedAt(instant: Instant) {
        storage.putStringSafe(KEY_SYNCED_AT, instant.toEpochMilliseconds().toString())
    }

    fun clearAll() {
        storage.removeSafe(KEY_MENU)
        storage.removeSafe(KEY_PERMISSIONS)
        storage.removeSafe(KEY_CONTEXTS)
        storage.removeSafe(KEY_HASHES)
        storage.removeSafe(KEY_SYNCED_AT)

        val screenKeys = getScreenKeys()
        for (key in screenKeys) {
            storage.removeSafe("$SCREEN_PREFIX$key")
        }
        storage.removeSafe(KEY_SCREEN_KEYS)
    }

    private fun loadAllScreens(): Map<String, ScreenDefinition> {
        val keys = getScreenKeys()
        val screens = mutableMapOf<String, ScreenDefinition>()
        for (key in keys) {
            val screenJson = storage.getStringSafe("$SCREEN_PREFIX$key")
            if (screenJson.isNotBlank()) {
                try {
                    screens[key] = json.decodeFromString(screenJson)
                } catch (_: Exception) {
                    // Skip corrupt entries
                }
            }
        }
        return screens
    }

    private fun getScreenKeys(): List<String> {
        val keysJson = storage.getStringSafe(KEY_SCREEN_KEYS)
        if (keysJson.isBlank()) return emptyList()
        return try {
            json.decodeFromString(keysJson)
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun addScreenKey(key: String) {
        val keys = getScreenKeys().toMutableSet()
        keys.add(key)
        storage.putStringSafe(KEY_SCREEN_KEYS, json.encodeToString(keys.toList()))
    }

    private fun removeScreenKey(key: String) {
        val keys = getScreenKeys().toMutableSet()
        keys.remove(key)
        storage.putStringSafe(KEY_SCREEN_KEYS, json.encodeToString(keys.toList()))
    }

    private fun updateHash(bucketKey: String, hash: String) {
        val hashes = getHashes().toMutableMap()
        hashes[bucketKey] = hash
        storage.putStringSafe(KEY_HASHES, json.encodeToString(hashes))
    }

    private fun removeHash(bucketKey: String) {
        val hashes = getHashes().toMutableMap()
        hashes.remove(bucketKey)
        storage.putStringSafe(KEY_HASHES, json.encodeToString(hashes))
    }
}

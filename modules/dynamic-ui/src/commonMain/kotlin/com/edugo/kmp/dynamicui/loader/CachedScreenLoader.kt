package com.edugo.kmp.dynamicui.loader

import com.edugo.kmp.dynamicui.cache.CacheConfig
import com.edugo.kmp.dynamicui.model.NavigationDefinition
import com.edugo.kmp.dynamicui.model.ScreenDefinition
import com.edugo.kmp.foundation.result.Result
import com.edugo.kmp.logger.Logger
import com.edugo.kmp.network.connectivity.NetworkObserver
import com.edugo.kmp.storage.SafeEduGoStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

class CachedScreenLoader(
    private val remote: ScreenLoader,
    private val storage: SafeEduGoStorage,
    private val cacheConfig: CacheConfig = CacheConfig(),
    private val networkObserver: NetworkObserver? = null,
    private val versionChecker: ScreenVersionChecker? = null,
    private val cacheDuration: Duration = 1.hours,
    private val maxMemoryEntries: Int = cacheConfig.maxScreenMemoryEntries,
    private val clock: Clock = Clock.System,
    private val logger: Logger? = null,
) : ScreenLoader {

    @Serializable
    private data class CacheEntry(
        val screen: ScreenDefinition,
        val cachedAt: Long,
        val version: Int = 1,
    )

    private val memoryCache = mutableMapOf<String, Pair<ScreenDefinition, Instant>>()
    private val mutex = Mutex()
    private var navCache: Pair<NavigationDefinition, Instant>? = null
    private val backgroundScope = CoroutineScope(Dispatchers.Default)

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    companion object {
        private const val INVALIDATED_AT_KEY = "screen.cache.__invalidatedAt"
    }

    private fun isValid(cachedAt: Instant, screenKey: String?, pattern: com.edugo.kmp.dynamicui.model.ScreenPattern? = null): Boolean {
        val ttl = cacheConfig.screenTtlFor(pattern, screenKey)
        if (clock.now() - cachedAt >= ttl) return false
        val invalidatedAt = getPersistedInvalidatedAt()
        if (invalidatedAt != null && cachedAt <= invalidatedAt) return false
        return true
    }

    private fun getPersistedInvalidatedAt(): Instant? {
        val millis = storage.getStringSafe(INVALIDATED_AT_KEY)
        return if (millis.isNotEmpty()) {
            try {
                Instant.fromEpochMilliseconds(millis.toLong())
            } catch (_: NumberFormatException) {
                null
            }
        } else null
    }

    override suspend fun loadScreen(
        screenKey: String,
        platform: String?,
    ): Result<ScreenDefinition> = mutex.withLock {
        // 1. Check memory cache
        memoryCache[screenKey]?.let { (screen, timestamp) ->
            if (isValid(timestamp, screenKey, screen.pattern)) {
                logger?.d("EduGo.Cache.Screen", "L1 HIT: $screenKey")
                launchVersionCheck(screenKey, screen.version)
                return Result.Success(screen)
            } else {
                memoryCache.remove(screenKey)
            }
        }

        // 2. Check persistent cache
        val cached = storage.getStringSafe("screen.cache.$screenKey")
        var staleScreen: ScreenDefinition? = null
        if (cached.isNotEmpty()) {
            try {
                val entry = json.decodeFromString<CacheEntry>(cached)
                val cachedAt = Instant.fromEpochMilliseconds(entry.cachedAt)
                if (isValid(cachedAt, screenKey, entry.screen.pattern)) {
                    logger?.d("EduGo.Cache.Screen", "L2 HIT: $screenKey")
                    putMemoryEntry(screenKey, entry.screen, cachedAt)
                    launchVersionCheck(screenKey, entry.version)
                    return Result.Success(entry.screen)
                }
                staleScreen = entry.screen
            } catch (_: Exception) {
                storage.removeSafe("screen.cache.$screenKey")
            }
        }

        // 3. If offline, return stale cache if available
        if (networkObserver != null && !networkObserver.isOnline) {
            if (staleScreen != null) {
                logger?.d("EduGo.Cache.Screen", "STALE (offline): $screenKey")
                return Result.Success(staleScreen)
            }
        }

        // 4. Load from remote
        logger?.d("EduGo.Cache.Screen", "REMOTE: $screenKey")
        val result = remote.loadScreen(screenKey, platform)
        if (result is Result.Success) {
            val screen = result.data
            val now = clock.now()
            putMemoryEntry(screenKey, screen, now)
            val entry = CacheEntry(screen, now.toEpochMilliseconds(), screen.version)
            storage.putStringSafe(
                "screen.cache.$screenKey",
                json.encodeToString(entry),
            )
            return result
        }

        // 5. Remote failed - try stale cache as fallback
        if (staleScreen != null) {
            logger?.d("EduGo.Cache.Screen", "STALE FALLBACK: $screenKey")
            return Result.Success(staleScreen)
        }

        // Also check memory for any stale entry (may have been removed from the map above)
        // but we already removed it. Check storage one more time for any parseable data.
        logger?.d("EduGo.Cache.Screen", "MISS: $screenKey")
        result
    }

    override suspend fun loadNavigation(): Result<NavigationDefinition> = mutex.withLock {
        navCache?.let { (def, ts) ->
            if (isValid(ts, null)) {
                return Result.Success(def)
            } else {
                navCache = null
            }
        }

        val result = remote.loadNavigation()
        if (result is Result.Success) {
            navCache = result.data to clock.now()
        }
        result
    }

    suspend fun clearCache() = mutex.withLock {
        memoryCache.clear()
        navCache = null
        val now = clock.now()
        storage.putStringSafe(INVALIDATED_AT_KEY, now.toEpochMilliseconds().toString())
    }

    suspend fun evict(screenKey: String) = mutex.withLock {
        memoryCache.remove(screenKey)
        storage.removeSafe("screen.cache.$screenKey")
    }

    /**
     * Pre-fetches screen configs in background for offline availability.
     * Extracts all screenKeys from navigation items and registered contract keys,
     * then loads each one (which caches it in L1+L2).
     */
    fun prefetchScreens(screenKeys: List<String>) {
        if (networkObserver?.isOnline == false) return
        backgroundScope.launch {
            for (key in screenKeys) {
                try {
                    loadScreen(key)
                } catch (_: Exception) {
                    // Non-critical, continue with next
                }
            }
        }
    }

    private fun putMemoryEntry(key: String, screen: ScreenDefinition, timestamp: Instant) {
        if (memoryCache.size >= maxMemoryEntries) {
            val eldestKey = memoryCache.keys.first()
            memoryCache.remove(eldestKey)
        }
        memoryCache[key] = screen to timestamp
    }

    /**
     * Seeds the cache (L1 memory + L2 storage) from a pre-fetched bundle of screens.
     * Used by DataSyncService after full/delta sync to pre-populate the cache
     * so all screens are available immediately (including offline).
     */
    suspend fun seedFromBundle(screens: Map<String, ScreenDefinition>) = mutex.withLock {
        val now = clock.now()
        val nowMillis = now.toEpochMilliseconds()

        // Phase 1: Seed memory cache (fast, sequential - HashMap not thread-safe)
        for ((key, screen) in screens) {
            putMemoryEntry(key, screen, now)
        }

        // Phase 2: Serialize & persist to storage in parallel (CPU-bound)
        withContext(Dispatchers.Default) {
            screens.map { (key, screen) ->
                async {
                    val entry = CacheEntry(screen, nowMillis, screen.version)
                    storage.putStringSafe(
                        "screen.cache.$key",
                        json.encodeToString(entry),
                    )
                }
            }.awaitAll()
        }
    }

    private fun launchVersionCheck(screenKey: String, cachedVersion: Int) {
        if (networkObserver?.isOnline != true || versionChecker == null) return
        backgroundScope.launch {
            try {
                val serverVersion = versionChecker.checkVersion(screenKey)
                if (serverVersion != null && serverVersion > cachedVersion) {
                    evict(screenKey)
                }
            } catch (_: Exception) {
                // Version check failure is non-critical
            }
        }
    }
}

/**
 * Interface for checking screen version against the server.
 * Implementation will call IAM Platform's version endpoint.
 */
fun interface ScreenVersionChecker {
    suspend fun checkVersion(screenKey: String): Int?
}

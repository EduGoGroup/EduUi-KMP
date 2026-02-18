package com.edugo.kmp.dynamicui.loader

import com.edugo.kmp.dynamicui.model.NavigationDefinition
import com.edugo.kmp.dynamicui.model.ScreenDefinition
import com.edugo.kmp.foundation.result.Result
import com.edugo.kmp.storage.SafeEduGoStorage
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
    private val cacheDuration: Duration = 1.hours,
    private val maxMemoryEntries: Int = 20,
    private val clock: Clock = Clock.System
) : ScreenLoader {

    @Serializable
    private data class CacheEntry(
        val screen: ScreenDefinition,
        val cachedAt: Long
    )

    private val memoryCache = LinkedHashMap<String, Pair<ScreenDefinition, Instant>>(
        maxMemoryEntries, 0.75f, true // accessOrder=true para LRU
    )
    private val mutex = Mutex()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    companion object {
        private const val INVALIDATED_AT_KEY = "screen.cache.__invalidatedAt"
    }

    private fun isValid(cachedAt: Instant): Boolean {
        if (clock.now() - cachedAt >= cacheDuration) return false
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
        platform: String?
    ): Result<ScreenDefinition> = mutex.withLock {
        // 1. Check memory cache
        memoryCache[screenKey]?.let { (screen, timestamp) ->
            if (isValid(timestamp)) {
                return Result.Success(screen)
            } else {
                memoryCache.remove(screenKey)
            }
        }

        // 2. Check persistent cache
        val cached = storage.getStringSafe("screen.cache.$screenKey")
        if (cached.isNotEmpty()) {
            try {
                val entry = json.decodeFromString<CacheEntry>(cached)
                val cachedAt = Instant.fromEpochMilliseconds(entry.cachedAt)
                if (isValid(cachedAt)) {
                    putMemoryEntry(screenKey, entry.screen, cachedAt)
                    return Result.Success(entry.screen)
                }
            } catch (_: Exception) {
                storage.removeSafe("screen.cache.$screenKey")
            }
        }

        // 3. Load from remote
        remote.loadScreen(screenKey, platform).also { result ->
            if (result is Result.Success) {
                val screen = result.data
                val now = clock.now()
                putMemoryEntry(screenKey, screen, now)
                val entry = CacheEntry(screen, now.toEpochMilliseconds())
                storage.putStringSafe(
                    "screen.cache.$screenKey",
                    json.encodeToString(entry)
                )
            }
        }
    }

    override suspend fun loadNavigation(): Result<NavigationDefinition> {
        return remote.loadNavigation()
    }

    suspend fun clearCache() = mutex.withLock {
        memoryCache.clear()
        val now = clock.now()
        storage.putStringSafe(INVALIDATED_AT_KEY, now.toEpochMilliseconds().toString())
    }

    suspend fun evict(screenKey: String) = mutex.withLock {
        memoryCache.remove(screenKey)
        storage.removeSafe("screen.cache.$screenKey")
    }

    private fun putMemoryEntry(key: String, screen: ScreenDefinition, timestamp: Instant) {
        if (memoryCache.size >= maxMemoryEntries) {
            val eldestKey = memoryCache.keys.first()
            memoryCache.remove(eldestKey)
        }
        memoryCache[key] = screen to timestamp
    }
}

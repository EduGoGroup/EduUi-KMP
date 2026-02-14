package com.edugo.kmp.dynamicui.loader

import com.edugo.kmp.dynamicui.model.NavigationDefinition
import com.edugo.kmp.dynamicui.model.ScreenDefinition
import com.edugo.kmp.foundation.result.Result
import com.edugo.kmp.storage.SafeEduGoStorage
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
    private val clock: Clock = Clock.System
) : ScreenLoader {

    @Serializable
    private data class CacheEntry(
        val screen: ScreenDefinition,
        val cachedAt: Long
    )

    private val memoryCache = mutableMapOf<String, Pair<ScreenDefinition, Instant>>()
    private var invalidatedAt: Instant? = null

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private fun isValid(cachedAt: Instant): Boolean {
        if (clock.now() - cachedAt >= cacheDuration) return false
        invalidatedAt?.let { if (cachedAt <= it) return false }
        return true
    }

    override suspend fun loadScreen(
        screenKey: String,
        platform: String?
    ): Result<ScreenDefinition> {
        // 1. Check memory cache
        memoryCache[screenKey]?.let { (screen, timestamp) ->
            if (isValid(timestamp)) {
                return Result.Success(screen)
            }
        }

        // 2. Check persistent cache
        val cached = storage.getStringSafe("screen.cache.$screenKey")
        if (cached.isNotEmpty()) {
            try {
                val entry = json.decodeFromString<CacheEntry>(cached)
                val cachedAt = Instant.fromEpochMilliseconds(entry.cachedAt)
                if (isValid(cachedAt)) {
                    memoryCache[screenKey] = entry.screen to cachedAt
                    return Result.Success(entry.screen)
                }
            } catch (_: Exception) {
                // Cache corrupted, fall through to remote
            }
        }

        // 3. Load from remote
        return remote.loadScreen(screenKey, platform).also { result ->
            if (result is Result.Success) {
                val screen = result.data
                val now = clock.now()
                memoryCache[screenKey] = screen to now
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

    fun clearCache() {
        memoryCache.clear()
        invalidatedAt = clock.now()
    }

    fun evict(screenKey: String) {
        memoryCache.remove(screenKey)
        storage.removeSafe("screen.cache.$screenKey")
    }
}

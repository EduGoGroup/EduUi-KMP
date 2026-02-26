package com.edugo.kmp.dynamicui.data

import com.edugo.kmp.dynamicui.model.DataConfig
import com.edugo.kmp.foundation.result.Result
import com.edugo.kmp.storage.SafeEduGoStorage
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class CachedDataLoader(
    private val remote: DataLoader,
    private val storage: SafeEduGoStorage,
    private val contextKeyProvider: () -> String = { "" },
    private val defaultCacheDuration: Duration = 5.minutes,
    private val maxMemoryEntries: Int = 30,
    private val clock: Clock = Clock.System
) : DataLoader {

    private data class CacheEntry(val data: DataPage, val timestamp: Instant)

    private val memoryCache = LinkedHashMap<String, CacheEntry>()
    private val mutex = Mutex()
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun loadData(
        endpoint: String,
        config: DataConfig,
        params: Map<String, String>
    ): Result<DataPage> {
        val key = buildCacheKey(endpoint, params)
        val ttl = config.refreshInterval?.let { it.seconds } ?: defaultCacheDuration

        return mutex.withLock {
            // 1. Memory cache (fresh)
            val cached = memoryCache[key]
            if (cached != null && (clock.now() - cached.timestamp) < ttl) {
                return@withLock Result.Success(cached.data)
            }

            // 2. Try remote
            val result = remote.loadData(endpoint, config, params)
            if (result is Result.Success) {
                updateMemoryCache(key, result.data)
                persistToStorage(key, result.data)
                return@withLock result
            }

            // 3. Network failed → stale cache (offline support)
            val stale = cached ?: loadFromStorage(key)
            if (stale != null) {
                return@withLock Result.Success(stale.data)
            }

            result
        }
    }

    override suspend fun submitData(
        endpoint: String,
        body: JsonObject,
        method: String
    ): Result<JsonObject?> {
        val result = remote.submitData(endpoint, body, method)
        if (result is Result.Success) {
            mutex.withLock {
                invalidateByPrefix(endpoint.substringBeforeLast("/"))
            }
        }
        return result
    }

    private fun buildCacheKey(endpoint: String, params: Map<String, String>): String {
        val ctx = contextKeyProvider()
        val paramsKey = params.entries.sortedBy { it.key }
            .joinToString("&") { "${it.key}=${it.value}" }
        return "data:$ctx:$endpoint:$paramsKey"
    }

    private fun updateMemoryCache(key: String, data: DataPage) {
        if (memoryCache.size >= maxMemoryEntries) {
            val eldestKey = memoryCache.keys.first()
            memoryCache.remove(eldestKey)
        }
        memoryCache[key] = CacheEntry(data, clock.now())
    }

    private fun persistToStorage(key: String, data: DataPage) {
        try {
            val storageKey = "data.cache.${key.hashCode()}"
            val payload = json.encodeToString(StorageCacheEntry(
                cacheKey = key,
                data = data,
                cachedAt = clock.now().toEpochMilliseconds()
            ))
            storage.putStringSafe(storageKey, payload)
        } catch (_: Exception) {
            // Storage write failure is non-critical
        }
    }

    private fun loadFromStorage(key: String): CacheEntry? {
        return try {
            val storageKey = "data.cache.${key.hashCode()}"
            val raw = storage.getStringSafe(storageKey)
            if (raw.isEmpty()) return null
            val entry = json.decodeFromString<StorageCacheEntry>(raw)
            if (entry.cacheKey != key) return null
            CacheEntry(entry.data, Instant.fromEpochMilliseconds(entry.cachedAt))
        } catch (_: Exception) {
            null
        }
    }

    private fun invalidateByPrefix(prefix: String) {
        val keysToRemove = memoryCache.keys.filter { it.contains(prefix) }
        keysToRemove.forEach { memoryCache.remove(it) }
    }

    suspend fun clearCache() = mutex.withLock {
        memoryCache.clear()
    }

    @kotlinx.serialization.Serializable
    private data class StorageCacheEntry(
        val cacheKey: String,
        val data: DataPage,
        val cachedAt: Long
    )
}

package com.edugo.kmp.dynamicui.data

import com.edugo.kmp.dynamicui.cache.CacheConfig
import com.edugo.kmp.dynamicui.model.DataConfig
import com.edugo.kmp.dynamicui.model.ScreenPattern
import com.edugo.kmp.dynamicui.offline.MutationQueue
import com.edugo.kmp.dynamicui.offline.MutationStatus
import com.edugo.kmp.dynamicui.offline.PendingMutation
import com.edugo.kmp.foundation.result.Result
import com.edugo.kmp.logger.Logger
import com.edugo.kmp.network.connectivity.NetworkObserver
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

/**
 * Result wrapper that includes staleness information.
 */
data class CachedDataResult(
    val data: DataPage,
    val isStale: Boolean = false,
)

class CachedDataLoader(
    private val remote: DataLoader,
    private val storage: SafeEduGoStorage,
    private val contextKeyProvider: () -> String = { "" },
    private val cacheConfig: CacheConfig = CacheConfig(),
    private val networkObserver: NetworkObserver? = null,
    private val mutationQueue: MutationQueue? = null,
    private val defaultCacheDuration: Duration = 5.minutes,
    private val maxMemoryEntries: Int = cacheConfig.maxDataMemoryEntries,
    private val clock: Clock = Clock.System,
    private val logger: Logger? = null,
) : DataLoader {

    private data class CacheEntry(val data: DataPage, val timestamp: Instant)

    private val memoryCache = LinkedHashMap<String, CacheEntry>()
    private val mutex = Mutex()
    private val json = Json { ignoreUnknownKeys = true }

    /** Load data with staleness information. */
    suspend fun loadDataWithStaleness(
        endpoint: String,
        config: DataConfig,
        params: Map<String, String> = emptyMap(),
        screenPattern: ScreenPattern? = null,
        screenKey: String? = null,
    ): Result<CachedDataResult> {
        val key = buildCacheKey(endpoint, params)
        val ttl = resolveTtl(config, screenPattern, screenKey)

        return mutex.withLock {
            // If offline, go straight to cache
            if (networkObserver != null && !networkObserver.isOnline) {
                val cached = memoryCache[key] ?: loadFromStorage(key)
                if (cached != null) {
                    logger?.d("EduGo.Cache.Data", "STALE (offline): $endpoint")
                    return@withLock Result.Success(CachedDataResult(cached.data, isStale = true))
                }
                logger?.d("EduGo.Cache.Data", "MISS (offline): $endpoint")
                return@withLock Result.Failure("Sin conexión y sin datos en caché")
            }

            // 1. Memory cache (fresh)
            val cached = memoryCache[key]
            if (cached != null && (clock.now() - cached.timestamp) < ttl) {
                logger?.d("EduGo.Cache.Data", "L1 HIT: $endpoint")
                return@withLock Result.Success(CachedDataResult(cached.data, isStale = false))
            }

            // 2. Try remote
            logger?.d("EduGo.Cache.Data", "REMOTE: $endpoint")
            val result = remote.loadData(endpoint, config, params)
            if (result is Result.Success) {
                updateMemoryCache(key, result.data)
                persistToStorage(key, result.data)
                return@withLock Result.Success(CachedDataResult(result.data, isStale = false))
            }

            // 3. Network failed → stale cache (offline fallback)
            val stale = cached ?: loadFromStorage(key)
            if (stale != null) {
                logger?.d("EduGo.Cache.Data", "STALE FALLBACK: $endpoint")
                return@withLock Result.Success(CachedDataResult(stale.data, isStale = true))
            }

            // 4. No cache at all
            logger?.d("EduGo.Cache.Data", "MISS: $endpoint")
            @Suppress("UNCHECKED_CAST")
            result as Result<CachedDataResult>
        }
    }

    override suspend fun loadData(
        endpoint: String,
        config: DataConfig,
        params: Map<String, String>,
    ): Result<DataPage> {
        val key = buildCacheKey(endpoint, params)
        val ttl = resolveTtl(config, null, null)

        return mutex.withLock {
            // If offline, go straight to cache
            if (networkObserver != null && !networkObserver.isOnline) {
                val cached = memoryCache[key] ?: loadFromStorage(key)
                if (cached != null) {
                    logger?.d("EduGo.Cache.Data", "STALE (offline): $endpoint")
                    return@withLock Result.Success(cached.data)
                }
                logger?.d("EduGo.Cache.Data", "MISS (offline): $endpoint")
                return@withLock Result.Failure("Sin conexión y sin datos en caché")
            }

            // 1. Memory cache (fresh)
            val cached = memoryCache[key]
            if (cached != null && (clock.now() - cached.timestamp) < ttl) {
                logger?.d("EduGo.Cache.Data", "L1 HIT: $endpoint")
                return@withLock Result.Success(cached.data)
            }

            // 2. Try remote
            logger?.d("EduGo.Cache.Data", "REMOTE: $endpoint")
            val result = remote.loadData(endpoint, config, params)
            if (result is Result.Success) {
                updateMemoryCache(key, result.data)
                persistToStorage(key, result.data)
                return@withLock result
            }

            // 3. Network failed → stale cache (offline support)
            val stale = cached ?: loadFromStorage(key)
            if (stale != null) {
                logger?.d("EduGo.Cache.Data", "STALE FALLBACK: $endpoint")
                return@withLock Result.Success(stale.data)
            }

            logger?.d("EduGo.Cache.Data", "MISS: $endpoint")
            result
        }
    }

    override suspend fun submitData(
        endpoint: String,
        body: JsonObject,
        method: String,
    ): Result<JsonObject?> {
        // If offline, enqueue and return optimistic success
        if (networkObserver != null && !networkObserver.isOnline) {
            if (mutationQueue != null) {
                mutationQueue.enqueue(
                    PendingMutation(
                        id = generateMutationId(),
                        endpoint = endpoint,
                        method = method,
                        body = body,
                        createdAt = clock.now().toEpochMilliseconds(),
                    ),
                )
                return Result.Success(null)
            }
            return Result.Failure("Sin conexión")
        }

        val result = remote.submitData(endpoint, body, method)
        if (result is Result.Success) {
            mutex.withLock {
                invalidateByPrefix(endpoint.substringBeforeLast("/"))
            }
        } else if (result is Result.Failure && mutationQueue != null) {
            // Network failed, enqueue for retry
            mutationQueue.enqueue(
                PendingMutation(
                    id = generateMutationId(),
                    endpoint = endpoint,
                    method = method,
                    body = body,
                    createdAt = clock.now().toEpochMilliseconds(),
                ),
            )
            return Result.Success(null)
        }
        return result
    }

    private fun resolveTtl(config: DataConfig, screenPattern: ScreenPattern?, screenKey: String?): Duration {
        return config.refreshInterval?.let { it.seconds }
            ?: cacheConfig.dataTtlFor(screenPattern, screenKey)
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
            val payload = json.encodeToString(
                StorageCacheEntry(
                    cacheKey = key,
                    data = data,
                    cachedAt = clock.now().toEpochMilliseconds(),
                ),
            )
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

    private fun generateMutationId(): String {
        return "mut-${clock.now().toEpochMilliseconds()}-${(0..9999).random()}"
    }

    @kotlinx.serialization.Serializable
    private data class StorageCacheEntry(
        val cacheKey: String,
        val data: DataPage,
        val cachedAt: Long,
    )
}

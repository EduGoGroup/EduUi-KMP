package com.edugo.kmp.dynamicui.offline

import com.edugo.kmp.storage.SafeEduGoStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MutationQueue(
    private val storage: SafeEduGoStorage,
    private val maxMutations: Int = 50,
) {
    private val mutex = Mutex()
    private val json = Json { ignoreUnknownKeys = true }
    private val mutations = mutableListOf<PendingMutation>()

    private val _pendingCount = MutableStateFlow(0)
    val pendingCount: StateFlow<Int> = _pendingCount.asStateFlow()

    companion object {
        private const val STORAGE_KEY = "offline.queue.mutations"
    }

    init {
        restoreFromStorage()
    }

    suspend fun enqueue(mutation: PendingMutation): Boolean = mutex.withLock {
        if (mutations.size >= maxMutations) return@withLock false
        if (isDuplicate(mutation)) return@withLock false
        mutations.add(mutation)
        persistAndNotify()
        true
    }

    suspend fun dequeue(): PendingMutation? = mutex.withLock {
        val item = mutations.firstOrNull { it.status == MutationStatus.PENDING }
        if (item != null) {
            val index = mutations.indexOf(item)
            mutations[index] = item.copy(status = MutationStatus.SYNCING)
            persistAndNotify()
        }
        item
    }

    suspend fun peek(): PendingMutation? = mutex.withLock {
        mutations.firstOrNull { it.status == MutationStatus.PENDING }
    }

    suspend fun markFailed(id: String) = mutex.withLock {
        val index = mutations.indexOfFirst { it.id == id }
        if (index >= 0) {
            val item = mutations[index]
            val newRetryCount = item.retryCount + 1
            val newStatus = if (newRetryCount >= item.maxRetries) MutationStatus.FAILED else MutationStatus.PENDING
            mutations[index] = item.copy(retryCount = newRetryCount, status = newStatus)
            persistAndNotify()
        }
    }

    suspend fun markConflicted(id: String) = mutex.withLock {
        val index = mutations.indexOfFirst { it.id == id }
        if (index >= 0) {
            mutations[index] = mutations[index].copy(status = MutationStatus.CONFLICTED)
            persistAndNotify()
        }
    }

    suspend fun remove(id: String) = mutex.withLock {
        mutations.removeAll { it.id == id }
        persistAndNotify()
    }

    suspend fun getAll(): List<PendingMutation> = mutex.withLock {
        mutations.toList()
    }

    suspend fun clear() = mutex.withLock {
        mutations.clear()
        persistAndNotify()
    }

    private fun isDuplicate(mutation: PendingMutation): Boolean {
        val hash = "${mutation.endpoint}|${mutation.method}|${mutation.body}"
        return mutations.any { existing ->
            existing.status == MutationStatus.PENDING &&
                "${existing.endpoint}|${existing.method}|${existing.body}" == hash
        }
    }

    private fun persistAndNotify() {
        _pendingCount.value = mutations.count { it.status == MutationStatus.PENDING }
        try {
            val data = json.encodeToString(mutations.toList())
            storage.putStringSafe(STORAGE_KEY, data)
        } catch (_: Exception) {
            // Storage write failure is non-critical
        }
    }

    private fun restoreFromStorage() {
        try {
            val data = storage.getStringSafe(STORAGE_KEY)
            if (data.isNotEmpty()) {
                val restored = json.decodeFromString<List<PendingMutation>>(data)
                mutations.addAll(restored)
                _pendingCount.value = mutations.count { it.status == MutationStatus.PENDING }
            }
        } catch (_: Exception) {
            // Corrupted data, start fresh
        }
    }
}

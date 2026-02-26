package com.edugo.kmp.dynamicui.offline

import com.edugo.kmp.dynamicui.data.DataLoader
import com.edugo.kmp.foundation.result.Result
import com.edugo.kmp.network.connectivity.NetworkObserver
import com.edugo.kmp.network.connectivity.NetworkStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SyncEngine(
    private val mutationQueue: MutationQueue,
    private val remoteDataLoader: DataLoader,
    private val networkObserver: NetworkObserver,
    private val conflictResolver: ConflictResolver,
) {
    sealed class SyncState {
        data object Idle : SyncState()
        data class Syncing(val current: Int, val total: Int) : SyncState()
        data object Completed : SyncState()
        data class Error(val message: String) : SyncState()
    }

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private var observeJob: Job? = null
    private var previousStatus: NetworkStatus = NetworkStatus.AVAILABLE

    fun start(scope: CoroutineScope) {
        observeJob?.cancel()
        previousStatus = networkObserver.status.value
        observeJob = scope.launch {
            networkObserver.status.collect { newStatus ->
                val wasOffline = previousStatus != NetworkStatus.AVAILABLE
                val isNowOnline = newStatus == NetworkStatus.AVAILABLE
                previousStatus = newStatus

                if (wasOffline && isNowOnline) {
                    processQueue()
                }
            }
        }
    }

    fun stop() {
        observeJob?.cancel()
        observeJob = null
    }

    suspend fun processQueue() {
        val all = mutationQueue.getAll()
        val pending = all.filter { it.status == MutationStatus.PENDING }
        if (pending.isEmpty()) {
            _syncState.value = SyncState.Idle
            return
        }

        val total = pending.size
        var processed = 0

        for (mutation in pending) {
            processed++
            _syncState.value = SyncState.Syncing(processed, total)

            val item = mutationQueue.dequeue() ?: break

            val result = remoteDataLoader.submitData(item.endpoint, item.body, item.method)

            when {
                result is Result.Success -> {
                    mutationQueue.remove(item.id)
                }
                isConflict(result) -> {
                    val resolution = conflictResolver.resolve(item, isEntityDeleted = isNotFound(result))
                    when (resolution) {
                        is ConflictResolver.Resolution.RetryWithoutCheck -> {
                            // Retry once without conflict check
                            val retry = remoteDataLoader.submitData(item.endpoint, item.body, item.method)
                            if (retry is Result.Success) {
                                mutationQueue.remove(item.id)
                            } else {
                                mutationQueue.markConflicted(item.id)
                            }
                        }
                        is ConflictResolver.Resolution.Skip -> {
                            mutationQueue.remove(item.id)
                        }
                        is ConflictResolver.Resolution.Failed -> {
                            mutationQueue.markFailed(item.id)
                        }
                    }
                }
                else -> {
                    // Retry with exponential backoff
                    val retried = retryWithBackoff(item)
                    if (!retried) {
                        mutationQueue.markFailed(item.id)
                    }
                }
            }
        }

        val remaining = mutationQueue.getAll().filter { it.status == MutationStatus.PENDING }
        _syncState.value = if (remaining.isEmpty()) SyncState.Completed else SyncState.Error("Some mutations failed")
    }

    private suspend fun retryWithBackoff(mutation: PendingMutation): Boolean {
        val maxRetries = 3
        var attempt = 0
        while (attempt < maxRetries) {
            delay(backoffDelay(attempt))
            attempt++
            val result = remoteDataLoader.submitData(mutation.endpoint, mutation.body, mutation.method)
            if (result is Result.Success) {
                mutationQueue.remove(mutation.id)
                return true
            }
        }
        return false
    }

    private fun backoffDelay(attempt: Int): Long {
        // 1s, 2s, 4s
        return 1000L * (1L shl attempt)
    }

    private fun isConflict(result: Result<*>): Boolean {
        return result is Result.Failure && result.error.contains("409", ignoreCase = true)
    }

    private fun isNotFound(result: Result<*>): Boolean {
        return result is Result.Failure && result.error.contains("404", ignoreCase = true)
    }
}

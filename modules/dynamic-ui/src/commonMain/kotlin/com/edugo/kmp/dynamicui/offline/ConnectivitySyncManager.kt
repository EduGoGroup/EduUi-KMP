package com.edugo.kmp.dynamicui.offline

import com.edugo.kmp.dynamicui.cache.RecentScreenTracker
import com.edugo.kmp.dynamicui.loader.CachedScreenLoader
import com.edugo.kmp.network.connectivity.NetworkObserver
import com.edugo.kmp.network.connectivity.NetworkStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class ConnectivitySyncManager(
    private val networkObserver: NetworkObserver,
    private val syncEngine: SyncEngine,
    private val recentScreenTracker: RecentScreenTracker,
    private val screenLoader: CachedScreenLoader,
) {
    sealed class SyncManagerEvent {
        data object ReloadCurrentScreen : SyncManagerEvent()
        data class SyncCompleted(val count: Int) : SyncManagerEvent()
    }

    private val _events = MutableSharedFlow<SyncManagerEvent>(extraBufferCapacity = 8)
    val events: SharedFlow<SyncManagerEvent> = _events.asSharedFlow()

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
                    onReconnect()
                }
            }
        }
    }

    fun stop() {
        observeJob?.cancel()
        observeJob = null
    }

    private suspend fun onReconnect() {
        // 1. Process pending mutations
        syncEngine.processQueue()

        // 2. Evict recently accessed screens from cache
        val recentKeys = recentScreenTracker.getRecentKeys()
        for (key in recentKeys) {
            screenLoader.evict(key)
        }

        // 3. Notify UI to reload current screen
        _events.emit(SyncManagerEvent.ReloadCurrentScreen)
    }
}

package com.edugo.kmp.dynamicui.sync.model

import kotlinx.datetime.Instant

sealed class SyncState {
    data object Idle : SyncState()
    data object Syncing : SyncState()
    data class Synced(val at: Instant) : SyncState()
    data class Stale(val cachedAt: Instant) : SyncState()
    data class Error(val message: String) : SyncState()
}

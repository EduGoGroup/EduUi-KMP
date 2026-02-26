package com.edugo.kmp.dynamicui.offline

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class ConflictResolver {

    sealed class ConflictEvent {
        data class Resolved(val mutationId: String, val strategy: String) : ConflictEvent()
        data class EntityDeleted(val mutationId: String, val endpoint: String) : ConflictEvent()
        data class Failed(val mutationId: String, val reason: String) : ConflictEvent()
    }

    sealed class Resolution {
        data object RetryWithoutCheck : Resolution()
        data object Skip : Resolution()
        data class Failed(val reason: String) : Resolution()
    }

    private val _events = MutableSharedFlow<ConflictEvent>(extraBufferCapacity = 16)
    val events: SharedFlow<ConflictEvent> = _events.asSharedFlow()

    suspend fun resolve(mutation: PendingMutation, isEntityDeleted: Boolean): Resolution {
        if (isEntityDeleted) {
            _events.emit(ConflictEvent.EntityDeleted(mutation.id, mutation.endpoint))
            return Resolution.Skip
        }

        // Last-write-wins: local change wins, retry without updated_at check
        _events.emit(ConflictEvent.Resolved(mutation.id, "last-write-wins"))
        return Resolution.RetryWithoutCheck
    }
}

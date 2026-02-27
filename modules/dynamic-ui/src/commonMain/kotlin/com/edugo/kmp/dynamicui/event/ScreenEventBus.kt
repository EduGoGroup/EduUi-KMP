package com.edugo.kmp.dynamicui.event

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed class ScreenDataEvent {
    data class DataChanged(val resource: String) : ScreenDataEvent()
    data class DataDeleted(val resource: String, val itemId: String) : ScreenDataEvent()
}

class ScreenEventBus {
    private val _events = MutableSharedFlow<ScreenDataEvent>(extraBufferCapacity = 16)
    val events: SharedFlow<ScreenDataEvent> = _events.asSharedFlow()

    suspend fun emit(event: ScreenDataEvent) {
        _events.emit(event)
    }
}

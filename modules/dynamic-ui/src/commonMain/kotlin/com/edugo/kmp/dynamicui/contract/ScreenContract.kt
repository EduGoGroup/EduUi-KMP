package com.edugo.kmp.dynamicui.contract

import com.edugo.kmp.dynamicui.model.DataConfig

interface ScreenContract {
    val screenKey: String
    val resource: String

    fun endpointFor(event: ScreenEvent, context: EventContext): String?

    fun permissionFor(event: ScreenEvent): String? = when (event) {
        ScreenEvent.LOAD_DATA, ScreenEvent.SEARCH,
        ScreenEvent.SELECT_ITEM, ScreenEvent.LOAD_MORE -> "$resource:read"
        ScreenEvent.SAVE_NEW -> "$resource:create"
        ScreenEvent.SAVE_EXISTING -> "$resource:update"
        ScreenEvent.DELETE -> "$resource:delete"
        ScreenEvent.REFRESH -> null
    }

    fun beforeRequest(event: ScreenEvent, url: String, context: EventContext): RequestConfig =
        RequestConfig(url = url)

    fun afterResponse(event: ScreenEvent, data: kotlinx.serialization.json.JsonObject): kotlinx.serialization.json.JsonObject = data

    fun customEventHandlers(): Map<String, CustomEventHandler> = emptyMap()

    fun dataConfig(): DataConfig = DataConfig()
}

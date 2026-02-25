package com.edugo.kmp.dynamicui.contract

import kotlinx.serialization.json.JsonObject

data class EventContext(
    val screenKey: String,
    val fieldValues: Map<String, String> = emptyMap(),
    val selectedItem: JsonObject? = null,
    val params: Map<String, String> = emptyMap()
)

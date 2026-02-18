package com.edugo.kmp.dynamicui.action

import kotlinx.serialization.json.JsonObject

data class ActionContext(
    val screenKey: String,
    val actionId: String,
    val config: JsonObject,
    val fieldValues: Map<String, String> = emptyMap(),
    val selectedItemId: String? = null,
    val selectedItem: JsonObject? = null
)

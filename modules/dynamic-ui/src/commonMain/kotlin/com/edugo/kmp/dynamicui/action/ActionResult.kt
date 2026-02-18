package com.edugo.kmp.dynamicui.action

import kotlinx.serialization.json.JsonObject

sealed class ActionResult {
    data class Success(
        val message: String? = null,
        val data: JsonObject? = null
    ) : ActionResult()

    data class NavigateTo(
        val screenKey: String,
        val params: Map<String, String> = emptyMap()
    ) : ActionResult()

    data class Error(
        val message: String,
        val retry: Boolean = false
    ) : ActionResult()

    data object Logout : ActionResult()

    data object Cancelled : ActionResult()
}

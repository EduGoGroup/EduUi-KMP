package com.edugo.kmp.dynamicui.contract

import kotlinx.serialization.json.JsonObject

sealed class EventResult {
    data class Success(
        val message: String? = null,
        val data: JsonObject? = null
    ) : EventResult()

    data class NavigateTo(
        val screenKey: String,
        val params: Map<String, String> = emptyMap()
    ) : EventResult()

    data class Error(
        val message: String,
        val retry: Boolean = false
    ) : EventResult()

    data object PermissionDenied : EventResult()

    data object Logout : EventResult()

    data object Cancelled : EventResult()

    data object NoOp : EventResult()

    data class SubmitTo(
        val endpoint: String,
        val method: String = "POST",
        val fieldValues: Map<String, String> = emptyMap()
    ) : EventResult()

    data class PendingDelete(
        val itemId: String,
        val endpoint: String,
        val method: String = "DELETE",
        val message: String = "Elemento eliminado"
    ) : EventResult()
}

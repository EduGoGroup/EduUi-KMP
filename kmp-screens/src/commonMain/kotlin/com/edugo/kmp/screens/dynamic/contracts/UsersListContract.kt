package com.edugo.kmp.screens.dynamic.contracts

import com.edugo.kmp.dynamicui.contract.CustomEventHandler
import com.edugo.kmp.dynamicui.contract.EventContext
import com.edugo.kmp.dynamicui.contract.EventResult
import com.edugo.kmp.dynamicui.model.DataConfig
import kotlinx.serialization.json.jsonPrimitive

class UsersListContract : BaseCrudContract(
    apiPrefix = "admin:",
    basePath = "/api/v1/users",
    resource = "users"
) {
    override val screenKey = "users-list"

    override fun dataConfig() = DataConfig(
        fieldMapping = mapOf(
            "title" to "full_name",
            "subtitle" to "email",
            "status" to "is_active"
        )
    )

    override fun customEventHandlers(): Map<String, CustomEventHandler> = mapOf(
        "select-item" to SelectItemHandler()
    )

    private class SelectItemHandler : CustomEventHandler {
        override val eventId = "select-item"
        override val requiredPermission: String? = "users:read"

        override suspend fun execute(context: EventContext): EventResult {
            val id = context.selectedItem?.get("id")?.jsonPrimitive?.content
                ?: return EventResult.Error("No user selected")
            return EventResult.NavigateTo("user-detail", mapOf("id" to id))
        }
    }
}

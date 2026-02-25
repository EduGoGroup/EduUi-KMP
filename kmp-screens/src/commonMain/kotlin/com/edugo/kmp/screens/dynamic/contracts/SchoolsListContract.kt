package com.edugo.kmp.screens.dynamic.contracts

import com.edugo.kmp.dynamicui.contract.CustomEventHandler
import com.edugo.kmp.dynamicui.contract.EventContext
import com.edugo.kmp.dynamicui.contract.EventResult
import com.edugo.kmp.dynamicui.model.DataConfig
import kotlinx.serialization.json.jsonPrimitive

class SchoolsListContract : BaseCrudContract(
    apiPrefix = "admin:",
    basePath = "/api/v1/schools",
    resource = "schools"
) {
    override val screenKey = "schools-list"

    override fun dataConfig() = DataConfig(
        fieldMapping = mapOf(
            "title" to "name",
            "subtitle" to "city",
            "status" to "is_active"
        )
    )

    override fun customEventHandlers(): Map<String, CustomEventHandler> = mapOf(
        "select-item" to SelectItemHandler(),
        "navigate-to-form" to NavigateToFormHandler()
    )

    private class SelectItemHandler : CustomEventHandler {
        override val eventId = "select-item"
        override val requiredPermission: String? = "schools:read"

        override suspend fun execute(context: EventContext): EventResult {
            val id = context.selectedItem?.get("id")?.jsonPrimitive?.content
                ?: return EventResult.Error("No school selected")
            return EventResult.NavigateTo("schools-form", mapOf("id" to id))
        }
    }

    private class NavigateToFormHandler : CustomEventHandler {
        override val eventId = "navigate-to-form"
        override val requiredPermission: String? = "schools:create"

        override suspend fun execute(context: EventContext): EventResult {
            return EventResult.NavigateTo("schools-form")
        }
    }
}

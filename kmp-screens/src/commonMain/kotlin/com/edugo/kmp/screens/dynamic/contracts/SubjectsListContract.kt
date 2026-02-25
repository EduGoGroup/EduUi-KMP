package com.edugo.kmp.screens.dynamic.contracts

import com.edugo.kmp.dynamicui.contract.CustomEventHandler
import com.edugo.kmp.dynamicui.contract.EventContext
import com.edugo.kmp.dynamicui.contract.EventResult
import com.edugo.kmp.dynamicui.model.DataConfig

class SubjectsListContract : BaseCrudContract(
    apiPrefix = "admin:",
    basePath = "/api/v1/subjects",
    resource = "subjects"
) {
    override val screenKey = "subjects-list"

    override fun dataConfig() = DataConfig(
        fieldMapping = mapOf(
            "title" to "name",
            "subtitle" to "description",
            "status" to "is_active"
        )
    )

    override fun customEventHandlers(): Map<String, CustomEventHandler> = mapOf(
        "navigate-to-form" to NavigateToFormHandler()
    )

    private class NavigateToFormHandler : CustomEventHandler {
        override val eventId = "navigate-to-form"
        override val requiredPermission: String? = "subjects:create"

        override suspend fun execute(context: EventContext): EventResult {
            return EventResult.NavigateTo("subjects-form")
        }
    }
}

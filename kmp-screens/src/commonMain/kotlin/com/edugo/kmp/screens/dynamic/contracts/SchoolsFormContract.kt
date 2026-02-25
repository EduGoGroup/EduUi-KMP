package com.edugo.kmp.screens.dynamic.contracts

import com.edugo.kmp.dynamicui.contract.CustomEventHandler
import com.edugo.kmp.dynamicui.contract.EventContext
import com.edugo.kmp.dynamicui.contract.EventResult
import com.edugo.kmp.dynamicui.contract.ScreenEvent

class SchoolsFormContract : BaseCrudContract(
    apiPrefix = "admin:",
    basePath = "/api/v1/schools",
    resource = "schools"
) {
    override val screenKey = "schools-form"

    override fun endpointFor(event: ScreenEvent, context: EventContext): String? {
        val id = context.params["id"]
        return when (event) {
            ScreenEvent.LOAD_DATA -> if (id != null) "admin:/api/v1/schools/$id" else null
            else -> super.endpointFor(event, context)
        }
    }

    override fun customEventHandlers(): Map<String, CustomEventHandler> = mapOf(
        "submit-form" to SubmitFormHandler(),
        "go-back" to GoBackHandler()
    )

    private class SubmitFormHandler : CustomEventHandler {
        override val eventId = "submit-form"
        override val requiredPermission: String? = "schools:create"

        override suspend fun execute(context: EventContext): EventResult {
            val id = context.params["id"]
            return if (id != null) {
                EventResult.SubmitTo(
                    endpoint = "admin:/api/v1/schools/$id",
                    method = "PUT",
                    fieldValues = context.fieldValues
                )
            } else {
                EventResult.SubmitTo(
                    endpoint = "admin:/api/v1/schools",
                    method = "POST",
                    fieldValues = context.fieldValues
                )
            }
        }
    }

    private class GoBackHandler : CustomEventHandler {
        override val eventId = "go-back"
        override val requiredPermission: String? = null

        override suspend fun execute(context: EventContext): EventResult {
            return EventResult.NavigateTo("schools-list")
        }
    }
}

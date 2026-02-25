package com.edugo.kmp.screens.dynamic.contracts

import com.edugo.kmp.dynamicui.contract.CustomEventHandler
import com.edugo.kmp.dynamicui.contract.EventContext
import com.edugo.kmp.dynamicui.contract.EventResult
import com.edugo.kmp.dynamicui.contract.ScreenContract
import com.edugo.kmp.dynamicui.contract.ScreenEvent

class AssessmentTakeContract : ScreenContract {
    override val screenKey = "assessment-take"
    override val resource = "assessments"

    override fun endpointFor(event: ScreenEvent, context: EventContext): String? = when (event) {
        ScreenEvent.LOAD_DATA -> {
            val id = context.params["id"]
            if (id != null) "/api/v1/assessments/$id" else null
        }
        else -> null
    }

    override fun customEventHandlers(): Map<String, CustomEventHandler> = mapOf(
        "submit-assessment" to SubmitAssessmentHandler()
    )

    private class SubmitAssessmentHandler : CustomEventHandler {
        override val eventId = "submit-assessment"
        override val requiredPermission: String? = "assessments:create"

        override suspend fun execute(context: EventContext): EventResult {
            val answers = context.fieldValues.filterKeys { it.startsWith("question_") }
            if (answers.isEmpty()) {
                return EventResult.Error("Please answer at least one question")
            }
            return EventResult.Success(message = "Assessment submitted successfully")
        }
    }
}

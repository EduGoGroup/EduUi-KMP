package com.edugo.kmp.dynamicui.handler.handlers

import com.edugo.kmp.dynamicui.action.ActionContext
import com.edugo.kmp.dynamicui.action.ActionResult
import com.edugo.kmp.dynamicui.handler.ScreenActionHandler
import com.edugo.kmp.dynamicui.model.ActionDefinition
import com.edugo.kmp.dynamicui.model.ActionType

class AssessmentTakeHandler : ScreenActionHandler {

    override val screenKeys = setOf("assessment-take")

    override fun canHandle(action: ActionDefinition): Boolean {
        return action.type == ActionType.SUBMIT_FORM
    }

    override suspend fun handle(
        action: ActionDefinition,
        context: ActionContext
    ): ActionResult {
        val answers = context.fieldValues.filter {
            it.key.startsWith("question_") || it.key.startsWith("answer_")
        }

        if (answers.isEmpty()) {
            return ActionResult.Error(message = "Please answer at least one question")
        }

        // En esta fase, retornamos navegacion directa.
        // El submit real lo maneja el SubmitFormHandler generico via la action config.
        // Este handler agrega validacion extra antes del submit.
        return ActionResult.Success(message = "Assessment submitted successfully")
    }
}

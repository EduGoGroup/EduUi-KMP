package com.edugo.kmp.dynamicui.action.handlers

import com.edugo.kmp.dynamicui.action.ActionContext
import com.edugo.kmp.dynamicui.action.ActionHandler
import com.edugo.kmp.dynamicui.action.ActionResult
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

class ConfirmHandler : ActionHandler {

    override suspend fun execute(context: ActionContext): ActionResult {
        val message = context.config["message"]?.jsonPrimitive?.contentOrNull
        val confirmActionId = context.config["confirmActionId"]?.jsonPrimitive?.contentOrNull

        return ActionResult.Success(
            message = message ?: "Confirm action: ${confirmActionId ?: context.actionId}"
        )
    }
}

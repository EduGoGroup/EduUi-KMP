package com.edugo.kmp.dynamicui.handler.handlers

import com.edugo.kmp.dynamicui.action.ActionContext
import com.edugo.kmp.dynamicui.action.ActionResult
import com.edugo.kmp.dynamicui.handler.ScreenActionHandler
import com.edugo.kmp.dynamicui.model.ActionDefinition
import com.edugo.kmp.dynamicui.model.ActionType

class GuardianHandler : ScreenActionHandler {
    override val screenKeys = setOf("dashboard-guardian", "children-list", "child-progress")

    override fun canHandle(action: ActionDefinition): Boolean {
        return action.type == ActionType.NAVIGATE || action.type == ActionType.REFRESH
    }

    override suspend fun handle(
        action: ActionDefinition,
        context: ActionContext
    ): ActionResult {
        return ActionResult.Success(message = null)
    }
}

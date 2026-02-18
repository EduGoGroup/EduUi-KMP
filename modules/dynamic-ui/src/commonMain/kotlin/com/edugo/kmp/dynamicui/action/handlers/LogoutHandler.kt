package com.edugo.kmp.dynamicui.action.handlers

import com.edugo.kmp.dynamicui.action.ActionContext
import com.edugo.kmp.dynamicui.action.ActionHandler
import com.edugo.kmp.dynamicui.action.ActionResult

class LogoutHandler : ActionHandler {

    override suspend fun execute(context: ActionContext): ActionResult {
        return ActionResult.Logout
    }
}

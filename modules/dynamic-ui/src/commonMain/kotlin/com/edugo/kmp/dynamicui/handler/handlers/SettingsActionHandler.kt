package com.edugo.kmp.dynamicui.handler.handlers

import com.edugo.kmp.auth.service.AuthService
import com.edugo.kmp.dynamicui.action.ActionContext
import com.edugo.kmp.dynamicui.action.ActionResult
import com.edugo.kmp.dynamicui.handler.ScreenActionHandler
import com.edugo.kmp.dynamicui.model.ActionDefinition
import com.edugo.kmp.dynamicui.model.ActionType

class SettingsActionHandler(
    private val authService: AuthService
) : ScreenActionHandler {

    override val screenKeys = setOf("app-settings")

    override fun canHandle(action: ActionDefinition): Boolean {
        return action.type == ActionType.LOGOUT ||
            action.type == ActionType.NAVIGATE_BACK ||
            (action.type == ActionType.CONFIRM && action.id == "theme_toggle")
    }

    override suspend fun handle(
        action: ActionDefinition,
        context: ActionContext
    ): ActionResult {
        return when (action.type) {
            ActionType.LOGOUT -> {
                authService.logout()
                ActionResult.Logout
            }
            ActionType.NAVIGATE_BACK -> ActionResult.NavigateTo("back")
            ActionType.CONFIRM -> {
                val message = when (action.id) {
                    "theme_toggle" -> "Theme changed"
                    else -> "Success"
                }
                ActionResult.Success(message = message)
            }
            else -> ActionResult.Error("Unhandled action: ${action.type}")
        }
    }
}

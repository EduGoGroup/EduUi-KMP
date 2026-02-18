package com.edugo.kmp.dynamicui.handler.handlers

import com.edugo.kmp.auth.model.LoginCredentials
import com.edugo.kmp.auth.model.LoginResult
import com.edugo.kmp.auth.service.AuthService
import com.edugo.kmp.dynamicui.action.ActionContext
import com.edugo.kmp.dynamicui.action.ActionResult
import com.edugo.kmp.dynamicui.handler.ScreenActionHandler
import com.edugo.kmp.dynamicui.model.ActionDefinition
import com.edugo.kmp.dynamicui.model.ActionType
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class LoginActionHandler(
    private val authService: AuthService
) : ScreenActionHandler {

    override val screenKeys = setOf("app-login", "app-login-es")

    override fun canHandle(action: ActionDefinition): Boolean {
        return action.type == ActionType.SUBMIT_FORM
    }

    override suspend fun handle(
        action: ActionDefinition,
        context: ActionContext
    ): ActionResult {
        val email = context.fieldValues["email"] ?: ""
        val password = context.fieldValues["password"] ?: ""

        if (email.isBlank() || password.isBlank()) {
            return ActionResult.Error("Email and password are required")
        }

        return when (val result = authService.login(LoginCredentials(email, password))) {
            is LoginResult.Success -> {
                val targetScreen = extractTargetScreen(action.config)
                ActionResult.NavigateTo(targetScreen)
            }
            is LoginResult.Error -> {
                ActionResult.Error(result.error.getUserFriendlyMessage())
            }
        }
    }

    private fun extractTargetScreen(config: JsonObject): String {
        return try {
            config["onSuccess"]
                ?.jsonObject?.get("config")
                ?.jsonObject?.get("target")
                ?.jsonPrimitive?.contentOrNull
                ?: DEFAULT_TARGET
        } catch (_: Exception) {
            DEFAULT_TARGET
        }
    }

    companion object {
        private const val DEFAULT_TARGET = "dashboard-home"
    }
}

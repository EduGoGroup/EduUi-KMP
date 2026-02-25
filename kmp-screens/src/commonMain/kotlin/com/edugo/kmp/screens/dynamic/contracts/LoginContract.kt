package com.edugo.kmp.screens.dynamic.contracts

import com.edugo.kmp.auth.model.LoginCredentials
import com.edugo.kmp.auth.model.LoginResult
import com.edugo.kmp.auth.service.AuthService
import com.edugo.kmp.dynamicui.contract.CustomEventHandler
import com.edugo.kmp.dynamicui.contract.EventContext
import com.edugo.kmp.dynamicui.contract.EventResult
import com.edugo.kmp.dynamicui.contract.ScreenContract
import com.edugo.kmp.dynamicui.contract.ScreenEvent

class LoginContract(
    private val authService: AuthService
) : ScreenContract {
    override val screenKey = "app-login"
    override val resource = "auth"

    override fun endpointFor(event: ScreenEvent, context: EventContext): String? = null

    override fun permissionFor(event: ScreenEvent): String? = null

    override fun customEventHandlers(): Map<String, CustomEventHandler> = mapOf(
        "submit-login" to SubmitLoginHandler(authService)
    )

    private class SubmitLoginHandler(
        private val authService: AuthService
    ) : CustomEventHandler {
        override val eventId = "submit-login"
        override val requiredPermission: String? = null

        override suspend fun execute(context: EventContext): EventResult {
            val email = context.fieldValues["email"] ?: ""
            val password = context.fieldValues["password"] ?: ""

            if (email.isBlank() || password.isBlank()) {
                return EventResult.Error("Email and password are required")
            }

            return when (val result = authService.login(LoginCredentials(email, password))) {
                is LoginResult.Success -> EventResult.NavigateTo("dashboard-home")
                is LoginResult.Error -> EventResult.Error(result.error.getUserFriendlyMessage())
            }
        }
    }
}

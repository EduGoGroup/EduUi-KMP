package com.edugo.kmp.screens.dynamic.contracts

import com.edugo.kmp.auth.service.AuthService
import com.edugo.kmp.dynamicui.contract.CustomEventHandler
import com.edugo.kmp.dynamicui.contract.EventContext
import com.edugo.kmp.dynamicui.contract.EventResult
import com.edugo.kmp.dynamicui.contract.ScreenContract
import com.edugo.kmp.dynamicui.contract.ScreenEvent

class SettingsContract(
    private val authService: AuthService
) : ScreenContract {
    override val screenKey = "app-settings"
    override val resource = "settings"

    override fun endpointFor(event: ScreenEvent, context: EventContext): String? = null

    override fun permissionFor(event: ScreenEvent): String? = null

    override fun customEventHandlers(): Map<String, CustomEventHandler> = mapOf(
        "logout" to LogoutHandler(authService),
        "navigate-back" to NavigateBackHandler(),
        "theme-toggle" to ThemeToggleHandler()
    )

    private class LogoutHandler(
        private val authService: AuthService
    ) : CustomEventHandler {
        override val eventId = "logout"
        override val requiredPermission: String? = null

        override suspend fun execute(context: EventContext): EventResult {
            authService.logout()
            return EventResult.Logout
        }
    }

    private class NavigateBackHandler : CustomEventHandler {
        override val eventId = "navigate-back"
        override val requiredPermission: String? = null

        override suspend fun execute(context: EventContext): EventResult {
            return EventResult.NavigateTo("back")
        }
    }

    private class ThemeToggleHandler : CustomEventHandler {
        override val eventId = "theme-toggle"
        override val requiredPermission: String? = null

        override suspend fun execute(context: EventContext): EventResult {
            return EventResult.Success(message = "Theme changed")
        }
    }
}

package com.edugo.kmp.dynamicui.handler.handlers

import com.edugo.kmp.auth.model.AuthError
import com.edugo.kmp.auth.model.AuthToken
import com.edugo.kmp.auth.model.AuthUserInfo
import com.edugo.kmp.auth.model.LoginCredentials
import com.edugo.kmp.auth.model.LoginResponse
import com.edugo.kmp.auth.model.LoginResult
import com.edugo.kmp.auth.model.LogoutResult
import com.edugo.kmp.auth.model.UserContext
import com.edugo.kmp.auth.service.AuthService
import com.edugo.kmp.auth.service.AuthState
import com.edugo.kmp.auth.token.TokenRefreshManager
import com.edugo.kmp.dynamicui.action.ActionContext
import com.edugo.kmp.dynamicui.action.ActionResult
import com.edugo.kmp.dynamicui.model.ActionDefinition
import com.edugo.kmp.dynamicui.model.ActionTrigger
import com.edugo.kmp.dynamicui.model.ActionType
import com.edugo.kmp.foundation.result.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class LoginActionHandlerTest {

    private fun createAction(type: ActionType, id: String = "submit_login"): ActionDefinition {
        return ActionDefinition(
            id = id,
            trigger = ActionTrigger.BUTTON_CLICK,
            type = type,
            config = JsonObject(emptyMap())
        )
    }

    private fun createContext(fields: Map<String, String> = emptyMap()): ActionContext {
        return ActionContext(
            screenKey = "app-login",
            actionId = "submit_login",
            config = JsonObject(emptyMap()),
            fieldValues = fields
        )
    }

    private fun createMockAuthService(
        loginResult: LoginResult = LoginResult.Success(
            LoginResponse.createTestResponse()
        )
    ): AuthService {
        return object : AuthService {
            override val authState: StateFlow<AuthState> = MutableStateFlow(AuthState.Unauthenticated)
            override val tokenRefreshManager: TokenRefreshManager
                get() = throw NotImplementedError()
            override val onSessionExpired: Flow<Unit> = emptyFlow()
            override val onLogout: Flow<LogoutResult> = emptyFlow()

            override suspend fun login(credentials: LoginCredentials): LoginResult = loginResult
            override suspend fun logout(): Result<Unit> = Result.Success(Unit)
            override suspend fun logoutWithDetails(forceLocal: Boolean): LogoutResult =
                LogoutResult.Success
            override suspend fun refreshAuthToken(): Result<AuthToken> = Result.Failure("Not implemented")
            override fun isAuthenticated(): Boolean = false
            override suspend fun getToken(): String? = null
            override suspend fun refreshToken(): String? = null
            override suspend fun isTokenExpired(): Boolean = true
            override fun getCurrentUser(): AuthUserInfo? = null
            override fun getCurrentAuthToken(): AuthToken? = null
            override suspend fun restoreSession() {}
        }
    }

    @Test
    fun canHandle_returns_true_for_SUBMIT_FORM() {
        val handler = LoginActionHandler(createMockAuthService())
        assertTrue(handler.canHandle(createAction(ActionType.SUBMIT_FORM)))
    }

    @Test
    fun canHandle_returns_false_for_NAVIGATE() {
        val handler = LoginActionHandler(createMockAuthService())
        assertFalse(handler.canHandle(createAction(ActionType.NAVIGATE)))
    }

    @Test
    fun canHandle_returns_false_for_LOGOUT() {
        val handler = LoginActionHandler(createMockAuthService())
        assertFalse(handler.canHandle(createAction(ActionType.LOGOUT)))
    }

    @Test
    fun screenKeys_contains_app_login() {
        val handler = LoginActionHandler(createMockAuthService())
        assertTrue("app-login" in handler.screenKeys)
        assertTrue("app-login-es" in handler.screenKeys)
    }

    @Test
    fun handle_SUBMIT_FORM_with_valid_credentials_returns_NavigateTo() = runTest {
        val handler = LoginActionHandler(createMockAuthService())
        val action = createAction(ActionType.SUBMIT_FORM)
        val context = createContext(mapOf("email" to "test@test.com", "password" to "password123"))

        val result = handler.handle(action, context)
        assertIs<ActionResult.NavigateTo>(result)
        assertEquals("dashboard-home", result.screenKey)
    }

    @Test
    fun handle_SUBMIT_FORM_with_empty_email_returns_Error() = runTest {
        val handler = LoginActionHandler(createMockAuthService())
        val action = createAction(ActionType.SUBMIT_FORM)
        val context = createContext(mapOf("email" to "", "password" to "password123"))

        val result = handler.handle(action, context)
        assertIs<ActionResult.Error>(result)
        assertEquals("Email and password are required", result.message)
    }

    @Test
    fun handle_SUBMIT_FORM_with_empty_password_returns_Error() = runTest {
        val handler = LoginActionHandler(createMockAuthService())
        val action = createAction(ActionType.SUBMIT_FORM)
        val context = createContext(mapOf("email" to "test@test.com", "password" to ""))

        val result = handler.handle(action, context)
        assertIs<ActionResult.Error>(result)
        assertEquals("Email and password are required", result.message)
    }

    @Test
    fun handle_SUBMIT_FORM_with_login_error_returns_Error() = runTest {
        val authService = createMockAuthService(
            loginResult = LoginResult.Error(AuthError.InvalidCredentials)
        )
        val handler = LoginActionHandler(authService)
        val action = createAction(ActionType.SUBMIT_FORM)
        val context = createContext(mapOf("email" to "test@test.com", "password" to "wrong"))

        val result = handler.handle(action, context)
        assertIs<ActionResult.Error>(result)
    }

    @Test
    fun handle_SUBMIT_FORM_with_missing_fields_returns_Error() = runTest {
        val handler = LoginActionHandler(createMockAuthService())
        val action = createAction(ActionType.SUBMIT_FORM)
        val context = createContext(emptyMap())

        val result = handler.handle(action, context)
        assertIs<ActionResult.Error>(result)
        assertEquals("Email and password are required", result.message)
    }
}

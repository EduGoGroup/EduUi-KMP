package com.edugo.kmp.auth.service

import com.edugo.kmp.auth.model.AuthToken
import com.edugo.kmp.auth.model.AuthUserInfo
import com.edugo.kmp.auth.model.AvailableContextsResponse
import com.edugo.kmp.auth.model.LoginCredentials
import com.edugo.kmp.auth.model.LoginResult
import com.edugo.kmp.auth.model.LogoutResult
import com.edugo.kmp.auth.model.UserContext
import com.edugo.kmp.auth.token.TokenRefreshManager
import com.edugo.kmp.foundation.result.Result
import com.edugo.kmp.network.interceptor.TokenProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Servicio principal de autenticación que gestiona el estado de sesión,
 * login/logout, y renovación de tokens.
 *
 * Implementa [TokenProvider] para integrarse directamente con
 * interceptores HTTP.
 */
public interface AuthService : TokenProvider {
    public val authState: StateFlow<AuthState>

    public val tokenRefreshManager: TokenRefreshManager

    public val onSessionExpired: Flow<Unit>

    public val onLogout: Flow<LogoutResult>

    public suspend fun login(credentials: LoginCredentials): LoginResult

    public suspend fun logout(): Result<Unit>

    public suspend fun logoutWithDetails(forceLocal: Boolean = true): LogoutResult

    public suspend fun refreshAuthToken(): Result<AuthToken>

    public fun isAuthenticated(): Boolean

    override suspend fun getToken(): String?

    override suspend fun isTokenExpired(): Boolean

    public fun getCurrentUser(): AuthUserInfo?

    public fun getCurrentAuthToken(): AuthToken?

    public suspend fun restoreSession()

    public suspend fun getAvailableContexts(): Result<AvailableContextsResponse>

    public suspend fun switchContext(schoolId: String): Result<UserContext>
}

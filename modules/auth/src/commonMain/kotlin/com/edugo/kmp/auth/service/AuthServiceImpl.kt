package com.edugo.kmp.auth.service

import com.edugo.kmp.auth.logging.AuthLogger
import com.edugo.kmp.auth.model.AuthError
import com.edugo.kmp.auth.model.AuthToken
import com.edugo.kmp.auth.model.AuthUserInfo
import com.edugo.kmp.auth.model.AvailableContextsResponse
import com.edugo.kmp.auth.model.LoginCredentials
import com.edugo.kmp.auth.model.LoginResult
import com.edugo.kmp.auth.model.LogoutResult
import com.edugo.kmp.auth.model.UserContext
import com.edugo.kmp.auth.repository.AuthRepository
import com.edugo.kmp.auth.throttle.RateLimiter
import com.edugo.kmp.auth.token.RefreshFailureReason
import com.edugo.kmp.auth.token.TokenRefreshConfig
import com.edugo.kmp.auth.token.TokenRefreshManager
import com.edugo.kmp.auth.token.TokenRefreshManagerImpl
import com.edugo.kmp.foundation.result.Result
import com.edugo.kmp.storage.SafeEduGoStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json

/**
 * Implementación de [AuthService] con gestión de estado reactivo y persistencia.
 *
 * Thread-safe usando [Mutex] de kotlinx.coroutines, compatible con KMP.
 */
public class AuthServiceImpl(
    private val repository: AuthRepository,
    private val storage: SafeEduGoStorage,
    private val json: Json = Json { ignoreUnknownKeys = true },
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
    refreshConfig: TokenRefreshConfig = TokenRefreshConfig.DEFAULT,
    private val loginRateLimiter: RateLimiter? = null,
    private val authLogger: AuthLogger? = null
) : AuthService {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val stateMutex = Mutex()

    override val tokenRefreshManager: TokenRefreshManager = TokenRefreshManagerImpl(
        repository = repository,
        storage = storage,
        config = refreshConfig,
        scope = scope,
        json = json
    )

    private val _onSessionExpired = MutableSharedFlow<Unit>(replay = 0)
    override val onSessionExpired: Flow<Unit> = _onSessionExpired.asSharedFlow()

    private val _onLogout = MutableSharedFlow<LogoutResult>(replay = 0)
    override val onLogout: Flow<LogoutResult> = _onLogout.asSharedFlow()

    init {
        scope.launch {
            tokenRefreshManager.onRefreshFailed.collect { reason ->
                when (reason) {
                    is RefreshFailureReason.TokenExpired,
                    is RefreshFailureReason.TokenRevoked,
                    is RefreshFailureReason.NoRefreshToken -> {
                        authLogger?.logSessionExpired(reason.toLogString())
                        clearSession()
                        _onSessionExpired.emit(Unit)
                    }
                    is RefreshFailureReason.NetworkError,
                    is RefreshFailureReason.ServerError -> {
                        authLogger?.logTokenRefresh(false)
                    }
                }
            }
        }

        scope.launch {
            tokenRefreshManager.onRefreshSuccess.collect { newToken ->
                authLogger?.logTokenRefresh(true)
                stateMutex.withLock {
                    val currentState = _authState.value
                    if (currentState is AuthState.Authenticated) {
                        saveToken(newToken)
                        _authState.value = currentState.copy(token = newToken)
                    }
                }
            }
        }
    }

    private suspend fun clearSession() {
        stateMutex.withLock {
            tokenRefreshManager.stopAutomaticRefresh()
            clearAuthData()
            _authState.value = AuthState.Unauthenticated
        }
    }

    public companion object {
        private const val AUTH_TOKEN_KEY = "auth_token"
        private const val AUTH_USER_KEY = "auth_user"
        private const val AUTH_CONTEXT_KEY = "auth_context"
    }

    override suspend fun login(credentials: LoginCredentials): LoginResult {
        return stateMutex.withLock {
            authLogger?.logLoginAttempt(credentials.email)
            _authState.value = AuthState.Loading

            val validationResult = credentials.validate()
            if (validationResult is Result.Failure) {
                _authState.value = AuthState.Unauthenticated
                authLogger?.logLoginFailure(credentials.email, "validation_failed")
                return LoginResult.Error(AuthError.InvalidCredentials)
            }

            val loginResult = loginRateLimiter?.execute { repository.login(credentials) } ?: repository.login(credentials)

            when (loginResult) {
                is Result.Success -> {
                    val loginResponse = loginResult.data
                    val authToken = loginResponse.toAuthToken()

                    saveAuthData(authToken, loginResponse.user, loginResponse.activeContext)

                    _authState.value = AuthState.Authenticated(
                        user = loginResponse.user,
                        token = authToken,
                        activeContext = loginResponse.activeContext
                    )

                    tokenRefreshManager.startAutomaticRefresh(authToken)
                    authLogger?.logLoginSuccess(credentials.email, loginResponse.user.id)

                    LoginResult.Success(loginResponse)
                }
                is Result.Failure -> {
                    _authState.value = AuthState.Unauthenticated
                    val authError = mapErrorToAuthError(loginResult.error)
                    authLogger?.logLoginFailure(credentials.email, loginResult.error)
                    LoginResult.Error(authError)
                }
                is Result.Loading -> {
                    _authState.value = AuthState.Unauthenticated
                    LoginResult.Error(AuthError.UnknownError("Unexpected loading state"))
                }
            }
        }
    }

    override suspend fun logout(): Result<Unit> {
        return stateMutex.withLock {
            val userId = getCurrentUser()?.id
            authLogger?.logLogout(userId)
            tokenRefreshManager.stopAutomaticRefresh()

            val token = getCurrentAuthToken()?.token

            if (token != null) {
                repository.logout(token)
            }

            clearAuthData()

            _authState.value = AuthState.Unauthenticated

            Result.Success(Unit)
        }
    }

    override suspend fun logoutWithDetails(forceLocal: Boolean): LogoutResult {
        return stateMutex.withLock {
            if (_authState.value is AuthState.Unauthenticated) {
                return@withLock LogoutResult.AlreadyLoggedOut
            }

            val token = getCurrentAuthToken()?.token
            var remoteError: String? = null

            if (token != null) {
                try {
                    val remoteResult = repository.logout(token)
                    if (remoteResult is Result.Failure) {
                        remoteError = remoteResult.error
                    }
                } catch (e: Exception) {
                    remoteError = e.message ?: "Network error"
                }
            }

            val shouldClearLocal = forceLocal || remoteError == null

            if (shouldClearLocal) {
                clearAllSessionData()
            }

            val result = when {
                remoteError == null -> LogoutResult.Success
                shouldClearLocal -> LogoutResult.PartialSuccess(remoteError)
                else -> LogoutResult.PartialSuccess(remoteError)
            }

            if (shouldClearLocal) {
                _onLogout.emit(result)
            }

            result
        }
    }

    private suspend fun clearAllSessionData() {
        tokenRefreshManager.stopAutomaticRefresh()
        tokenRefreshManager.cancelPendingRefresh()

        storage.removeSafe(AUTH_TOKEN_KEY)
        storage.removeSafe(AUTH_USER_KEY)

        _authState.value = AuthState.Unauthenticated
    }

    override suspend fun refreshAuthToken(): Result<AuthToken> {
        val result = tokenRefreshManager.forceRefresh()

        if (result is Result.Success) {
            stateMutex.withLock {
                val currentState = _authState.value
                if (currentState is AuthState.Authenticated) {
                    _authState.value = currentState.copy(token = result.data)
                }
            }
        }

        return result
    }

    override suspend fun refreshToken(): String? {
        return when (val result = refreshAuthToken()) {
            is Result.Success -> result.data.token
            is Result.Failure -> null
            is Result.Loading -> null
        }
    }

    override fun isAuthenticated(): Boolean {
        return _authState.value is AuthState.Authenticated
    }

    override suspend fun getToken(): String? {
        val token = getCurrentAuthToken() ?: return null

        if (token.isExpired()) {
            return refreshToken()
        }

        return token.token
    }

    override suspend fun isTokenExpired(): Boolean {
        return getCurrentAuthToken()?.isExpired() ?: true
    }

    override fun getCurrentUser(): AuthUserInfo? {
        return when (val state = _authState.value) {
            is AuthState.Authenticated -> state.user
            else -> null
        }
    }

    override fun getCurrentAuthToken(): AuthToken? {
        return when (val state = _authState.value) {
            is AuthState.Authenticated -> state.token
            else -> null
        }
    }

    override suspend fun restoreSession() {
        // Leer datos del storage con mutex
        val tokenJson: String
        val userJson: String
        val contextJson: String
        stateMutex.withLock {
            tokenJson = storage.getStringSafe(AUTH_TOKEN_KEY, "")
            userJson = storage.getStringSafe(AUTH_USER_KEY, "")
            contextJson = storage.getStringSafe(AUTH_CONTEXT_KEY, "")
        }

        if (tokenJson.isBlank() || userJson.isBlank() || contextJson.isBlank()) return

        try {
            val token = json.decodeFromString<AuthToken>(tokenJson)
            val user = json.decodeFromString<AuthUserInfo>(userJson)
            val context = json.decodeFromString<UserContext>(contextJson)

            if (!token.isExpired()) {
                stateMutex.withLock {
                    _authState.value = AuthState.Authenticated(user, token, context)
                    tokenRefreshManager.startAutomaticRefresh(token)
                    authLogger?.logSessionRestored(user.id)
                }
            } else if (token.hasRefreshToken()) {
                // forceRefresh FUERA del mutex para evitar deadlock
                // (onRefreshSuccess collector también necesita el mutex)
                val refreshResult = tokenRefreshManager.forceRefresh()
                stateMutex.withLock {
                    if (refreshResult is Result.Success) {
                        val newToken = refreshResult.data
                        _authState.value = AuthState.Authenticated(user, newToken, context)
                        tokenRefreshManager.startAutomaticRefresh(newToken)
                    } else {
                        clearAuthData()
                        _authState.value = AuthState.Unauthenticated
                    }
                }
            } else {
                stateMutex.withLock {
                    clearAuthData()
                    _authState.value = AuthState.Unauthenticated
                }
            }
        } catch (_: Exception) {
            stateMutex.withLock {
                clearAuthData()
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    private fun saveAuthData(token: AuthToken, user: AuthUserInfo, context: UserContext) {
        saveToken(token)
        saveUser(user)
        saveContext(context)
    }

    private fun saveToken(token: AuthToken) {
        val tokenJson = json.encodeToString(token)
        storage.putStringSafe(AUTH_TOKEN_KEY, tokenJson)
    }

    private fun saveUser(user: AuthUserInfo) {
        val userJson = json.encodeToString(user)
        storage.putStringSafe(AUTH_USER_KEY, userJson)
    }

    private fun saveContext(context: UserContext) {
        val contextJson = json.encodeToString(context)
        storage.putStringSafe(AUTH_CONTEXT_KEY, contextJson)
    }

    private fun clearAuthData() {
        storage.removeSafe(AUTH_TOKEN_KEY)
        storage.removeSafe(AUTH_USER_KEY)
        storage.removeSafe(AUTH_CONTEXT_KEY)
    }

    override suspend fun getAvailableContexts(): Result<AvailableContextsResponse> {
        val token = getCurrentAuthToken()?.token
            ?: return Result.Failure("Not authenticated")

        return repository.getAvailableContexts(token)
    }

    override suspend fun switchContext(schoolId: String): Result<UserContext> {
        val token = getCurrentAuthToken()?.token
            ?: return Result.Failure("Not authenticated")

        return when (val result = repository.switchContext(token, schoolId)) {
            is Result.Success -> {
                val switchResponse = result.data
                val newToken = switchResponse.toAuthToken()
                val contextInfo = switchResponse.context

                // Build UserContext from SwitchContextInfo
                val newContext = UserContext(
                    roleId = "",
                    roleName = contextInfo?.role ?: "",
                    schoolId = contextInfo?.schoolId ?: schoolId,
                    schoolName = contextInfo?.schoolName ?: "",
                )

                stateMutex.withLock {
                    val currentState = _authState.value
                    if (currentState is AuthState.Authenticated) {
                        saveAuthData(newToken, currentState.user, newContext)
                        _authState.value = AuthState.Authenticated(
                            user = currentState.user,
                            token = newToken,
                            activeContext = newContext
                        )
                        tokenRefreshManager.startAutomaticRefresh(newToken)
                    }
                }

                Result.Success(newContext)
            }
            is Result.Failure -> Result.Failure(result.error)
            is Result.Loading -> Result.Failure("Unexpected loading state")
        }
    }

    private fun mapErrorToAuthError(error: String): AuthError {
        return when {
            error.contains("Rate limit", ignoreCase = true) ->
                AuthError.AccountLocked
            error.contains("Circuit breaker", ignoreCase = true) ->
                AuthError.NetworkError(error)
            error.contains("401") || error.contains("invalid credentials", ignoreCase = true) ->
                AuthError.InvalidCredentials
            error.contains("404") || error.contains("not found", ignoreCase = true) ->
                AuthError.UserNotFound
            error.contains("423") || error.contains("locked", ignoreCase = true) ->
                AuthError.AccountLocked
            error.contains("403") || error.contains("forbidden", ignoreCase = true) ||
                    error.contains("inactive", ignoreCase = true) ->
                AuthError.UserInactive
            error.contains("network", ignoreCase = true) ||
                    error.contains("timeout", ignoreCase = true) ||
                    error.contains("connection", ignoreCase = true) ->
                AuthError.NetworkError(error)
            else ->
                AuthError.UnknownError(error)
        }
    }
}

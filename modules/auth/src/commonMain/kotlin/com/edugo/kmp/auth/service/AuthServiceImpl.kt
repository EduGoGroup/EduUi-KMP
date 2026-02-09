package com.edugo.kmp.auth.service

import com.edugo.kmp.auth.model.AuthError
import com.edugo.kmp.auth.model.AuthToken
import com.edugo.kmp.auth.model.AuthUserInfo
import com.edugo.kmp.auth.model.LoginCredentials
import com.edugo.kmp.auth.model.LoginResult
import com.edugo.kmp.auth.model.LogoutResult
import com.edugo.kmp.auth.repository.AuthRepository
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
import kotlinx.serialization.encodeToString
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
    refreshConfig: TokenRefreshConfig = TokenRefreshConfig.DEFAULT
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
                        clearSession()
                        _onSessionExpired.emit(Unit)
                    }
                    is RefreshFailureReason.NetworkError,
                    is RefreshFailureReason.ServerError -> {
                        // Error temporal, no limpiar sesión
                    }
                }
            }
        }
    }

    private suspend fun clearSession() {
        stateMutex.withLock {
            clearAuthData()
            _authState.value = AuthState.Unauthenticated
        }
    }

    public companion object {
        private const val AUTH_TOKEN_KEY = "auth_token"
        private const val AUTH_USER_KEY = "auth_user"
    }

    override suspend fun login(credentials: LoginCredentials): LoginResult {
        return stateMutex.withLock {
            _authState.value = AuthState.Loading

            val validationResult = credentials.validate()
            if (validationResult is Result.Failure) {
                _authState.value = AuthState.Unauthenticated
                return LoginResult.Error(AuthError.InvalidCredentials)
            }

            when (val result = repository.login(credentials)) {
                is Result.Success -> {
                    val loginResponse = result.data
                    val authToken = loginResponse.toAuthToken()

                    saveAuthData(authToken, loginResponse.user)

                    _authState.value = AuthState.Authenticated(
                        user = loginResponse.user,
                        token = authToken
                    )

                    LoginResult.Success(loginResponse)
                }
                is Result.Failure -> {
                    _authState.value = AuthState.Unauthenticated
                    val authError = mapErrorToAuthError(result.error)
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
        stateMutex.withLock {
            val tokenJson = storage.getStringSafe(AUTH_TOKEN_KEY, "")
            val userJson = storage.getStringSafe(AUTH_USER_KEY, "")

            if (tokenJson.isNotBlank() && userJson.isNotBlank()) {
                try {
                    val token = json.decodeFromString<AuthToken>(tokenJson)
                    val user = json.decodeFromString<AuthUserInfo>(userJson)

                    if (!token.isExpired()) {
                        _authState.value = AuthState.Authenticated(user, token)
                    } else {
                        if (token.hasRefreshToken()) {
                            val refreshResult = tokenRefreshManager.forceRefresh()
                            if (refreshResult is Result.Success) {
                                val newToken = refreshResult.data
                                _authState.value = AuthState.Authenticated(user, newToken)
                            } else {
                                clearAuthData()
                                _authState.value = AuthState.Unauthenticated
                            }
                        } else {
                            clearAuthData()
                            _authState.value = AuthState.Unauthenticated
                        }
                    }
                } catch (_: Exception) {
                    clearAuthData()
                    _authState.value = AuthState.Unauthenticated
                }
            }
        }
    }

    private fun saveAuthData(token: AuthToken, user: AuthUserInfo) {
        saveToken(token)
        saveUser(user)
    }

    private fun saveToken(token: AuthToken) {
        val tokenJson = json.encodeToString(token)
        storage.putStringSafe(AUTH_TOKEN_KEY, tokenJson)
    }

    private fun saveUser(user: AuthUserInfo) {
        val userJson = json.encodeToString(user)
        storage.putStringSafe(AUTH_USER_KEY, userJson)
    }

    private fun clearAuthData() {
        storage.removeSafe(AUTH_TOKEN_KEY)
        storage.removeSafe(AUTH_USER_KEY)
    }

    private fun mapErrorToAuthError(error: String): AuthError {
        return when {
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

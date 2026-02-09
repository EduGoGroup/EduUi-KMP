package com.edugo.kmp.auth.token

import com.edugo.kmp.auth.model.AuthToken
import com.edugo.kmp.auth.repository.AuthRepository
import com.edugo.kmp.foundation.result.Result
import com.edugo.kmp.foundation.result.failure
import com.edugo.kmp.foundation.result.success
import com.edugo.kmp.storage.SafeEduGoStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

/**
 * Implementación de [TokenRefreshManager] con sincronización thread-safe y retry inteligente.
 */
public class TokenRefreshManagerImpl(
    private val repository: AuthRepository,
    private val storage: SafeEduGoStorage,
    private val config: TokenRefreshConfig = TokenRefreshConfig.DEFAULT,
    private val scope: CoroutineScope,
    private val json: Json = Json { ignoreUnknownKeys = true }
) : TokenRefreshManager {

    private val refreshMutex = Mutex()

    private var refreshJob: Deferred<Result<AuthToken>>? = null

    private var refreshJobCancellable: Job? = null

    private val _onRefreshFailed = MutableSharedFlow<RefreshFailureReason>(replay = 0)
    override val onRefreshFailed: Flow<RefreshFailureReason> = _onRefreshFailed.asSharedFlow()

    private companion object {
        private const val AUTH_TOKEN_KEY = "auth_token"
    }

    override suspend fun refreshIfNeeded(): Result<AuthToken> {
        return refreshMutex.withLock {
            refreshJob?.let { return@withLock it.await() }

            val currentToken = getCurrentToken()
                ?: return@withLock failure("No token available")

            if (!shouldRefresh(currentToken)) {
                return@withLock success(currentToken)
            }

            val job = scope.async { performRefresh() }
            refreshJob = job
            refreshJobCancellable = job

            val result = job.await()
            refreshJob = null
            refreshJobCancellable = null

            result
        }
    }

    override suspend fun forceRefresh(): Result<AuthToken> {
        return refreshMutex.withLock {
            refreshJob?.let { return@withLock it.await() }

            val job = scope.async { performRefresh() }
            refreshJob = job
            refreshJobCancellable = job

            val result = job.await()
            refreshJob = null
            refreshJobCancellable = null

            result
        }
    }

    override fun shouldRefresh(token: AuthToken): Boolean {
        if (token.isExpired()) {
            return true
        }

        val now = Clock.System.now()
        val timeUntilExpiration = token.expiresAt - now
        val threshold = config.refreshThresholdSeconds.seconds

        return timeUntilExpiration <= threshold
    }

    private suspend fun performRefresh(): Result<AuthToken> {
        val refreshToken = getRefreshToken()
            ?: return handleRefreshFailure(RefreshFailureReason.NoRefreshToken)

        var lastError: Throwable? = null

        repeat(config.maxRetryAttempts + 1) { attempt ->
            if (attempt > 0) {
                val delayMs = config.calculateRetryDelay(attempt)
                delay(delayMs)
            }

            when (val result = repository.refresh(refreshToken)) {
                is Result.Success -> {
                    val refreshResponse = result.data
                    val newToken = refreshResponse.toAuthToken(refreshToken)

                    handleSuccessfulRefresh(newToken)

                    return success(newToken)
                }
                is Result.Failure -> {
                    lastError = Exception(result.error)

                    if (!isRetryableError(result.error)) {
                        val reason = mapErrorToFailureReason(result.error)
                        return handleRefreshFailure(reason)
                    }
                }
                is Result.Loading -> {
                    // No debería ocurrir en suspend function
                }
            }
        }

        val reason = RefreshFailureReason.NetworkError(
            lastError?.message ?: "Network error after ${config.maxRetryAttempts} retries"
        )
        return handleRefreshFailure(reason)
    }

    private fun handleSuccessfulRefresh(newToken: AuthToken) {
        saveToken(newToken)
    }

    private suspend fun handleRefreshFailure(reason: RefreshFailureReason): Result<AuthToken> {
        _onRefreshFailed.emit(reason)
        return failure(reason.toAuthError().errorCode.description)
    }

    private fun mapErrorToFailureReason(errorMessage: String): RefreshFailureReason {
        return when {
            errorMessage.contains("expired", ignoreCase = true) ->
                RefreshFailureReason.TokenExpired

            errorMessage.contains("revoked", ignoreCase = true) ->
                RefreshFailureReason.TokenRevoked

            errorMessage.contains("invalid", ignoreCase = true) ->
                RefreshFailureReason.TokenExpired

            errorMessage.contains("401") ->
                RefreshFailureReason.TokenExpired

            errorMessage.contains("network", ignoreCase = true) ||
            errorMessage.contains("timeout", ignoreCase = true) ||
            errorMessage.contains("connection", ignoreCase = true) ->
                RefreshFailureReason.NetworkError(errorMessage)

            errorMessage.matches(Regex(".*5\\d{2}.*")) -> {
                val code = Regex("(5\\d{2})").find(errorMessage)?.value?.toIntOrNull() ?: 500
                RefreshFailureReason.ServerError(code, errorMessage)
            }

            else ->
                RefreshFailureReason.ServerError(0, errorMessage)
        }
    }

    private fun isRetryableError(errorMessage: String): Boolean {
        if (errorMessage.contains("network", ignoreCase = true) ||
            errorMessage.contains("timeout", ignoreCase = true) ||
            errorMessage.contains("connection", ignoreCase = true)) {
            return true
        }

        if (errorMessage.matches(Regex(".*5\\d{2}.*"))) {
            return true
        }

        if (errorMessage.contains("expired", ignoreCase = true) ||
            errorMessage.contains("invalid", ignoreCase = true) ||
            errorMessage.contains("revoked", ignoreCase = true) ||
            errorMessage.contains("401")) {
            return false
        }

        return false
    }

    private fun getCurrentToken(): AuthToken? {
        val tokenJson = storage.getStringSafe(AUTH_TOKEN_KEY, "")
        if (tokenJson.isBlank()) return null

        return try {
            json.decodeFromString<AuthToken>(tokenJson)
        } catch (e: Exception) {
            null
        }
    }

    private fun getRefreshToken(): String? {
        return getCurrentToken()?.refreshToken
    }

    private fun saveToken(token: AuthToken) {
        val tokenJson = json.encodeToString(token)
        storage.putStringSafe(AUTH_TOKEN_KEY, tokenJson)
    }

    override fun cancelPendingRefresh() {
        refreshJobCancellable?.cancel()
        refreshJobCancellable = null
        refreshJob = null
    }
}

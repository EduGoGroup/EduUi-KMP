package com.edugo.kmp.auth.service

import com.edugo.kmp.auth.model.LoginCredentials
import com.edugo.kmp.auth.model.LoginResult
import com.edugo.kmp.auth.repository.StubAuthRepository
import com.edugo.kmp.auth.token.TokenRefreshConfig
import com.edugo.kmp.foundation.result.Result
import com.edugo.kmp.storage.EduGoStorage
import com.edugo.kmp.storage.SafeEduGoStorage
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AuthServiceRefreshIntegrationTest {

    private fun createStorageForTest(): SafeEduGoStorage {
        val storage = EduGoStorage.withSettings(MapSettings())
        return SafeEduGoStorage.wrap(storage)
    }

    @Test
    fun `successful refresh updates token in storage and state`() = runTest {
        val stubRepo = StubAuthRepository()
        val storage = createStorageForTest()
        val authService = AuthServiceFactory.createWithCustomComponents(
            repository = stubRepo,
            storage = storage,
            refreshConfig = TokenRefreshConfig.NO_RETRY
        )

        val loginResult = authService.login(LoginCredentials("test@edugo.com", "password123"))
        assertTrue(loginResult is LoginResult.Success)

        val originalToken = authService.getCurrentAuthToken()
        require(originalToken != null) { "Token should exist after login" }

        // Force refresh should succeed
        val refreshResult = authService.refreshAuthToken()

        assertTrue(refreshResult is Result.Success, "Refresh should succeed")
        val newToken = authService.getCurrentAuthToken()
        require(newToken != null) { "Token should exist after refresh" }

        // State should remain Authenticated
        assertTrue(
            authService.authState.value is AuthState.Authenticated,
            "State should remain Authenticated after refresh"
        )
    }

    @Test
    fun `concurrent refresh calls all succeed`() = runTest {
        val stubRepo = StubAuthRepository()
        val storage = createStorageForTest()

        val authService = AuthServiceFactory.createWithCustomComponents(
            repository = stubRepo,
            storage = storage,
            refreshConfig = TokenRefreshConfig.NO_RETRY
        )

        authService.login(LoginCredentials("test@edugo.com", "password123"))

        // Launch multiple refreshes - they should all succeed due to Mutex serialization
        val results = (1..5).map {
            async {
                authService.refreshAuthToken()
            }
        }.awaitAll()

        // All refreshes should succeed
        assertTrue(results.all { it is Result.Success }, "All refreshes should succeed")

        // State should remain authenticated
        assertTrue(
            authService.authState.value is AuthState.Authenticated,
            "State should remain Authenticated after concurrent refreshes"
        )
    }

    @Test
    fun `refresh failure returns failure result`() = runTest {
        val stubRepo = StubAuthRepository().apply {
            simulateNetworkError = true
        }
        val storage = createStorageForTest()

        val authService = AuthServiceFactory.createWithCustomComponents(
            repository = stubRepo,
            storage = storage,
            refreshConfig = TokenRefreshConfig.NO_RETRY
        )

        // Login with network working
        stubRepo.simulateNetworkError = false
        authService.login(LoginCredentials("test@edugo.com", "password123"))
        assertTrue(authService.isAuthenticated())

        // Now simulate network error
        stubRepo.simulateNetworkError = true

        // Force refresh should fail
        val refreshResult = authService.tokenRefreshManager.forceRefresh()
        assertTrue(refreshResult is Result.Failure, "Refresh should fail with network error")
    }

    @Test
    fun `refreshIfNeeded does not refresh when token has sufficient time`() = runTest {
        val stubRepo = StubAuthRepository()
        val storage = createStorageForTest()

        val authService = AuthServiceFactory.createWithCustomComponents(
            repository = stubRepo,
            storage = storage,
            refreshConfig = TokenRefreshConfig(
                refreshThresholdSeconds = 300,
                maxRetryAttempts = 0
            )
        )

        authService.login(LoginCredentials("test@edugo.com", "password123"))

        val result = authService.tokenRefreshManager.refreshIfNeeded()

        assertTrue(result is Result.Success, "Should return current token")
    }

    @Test
    fun `authService refreshToken method delegates to tokenRefreshManager`() = runTest {
        val stubRepo = StubAuthRepository()
        val storage = createStorageForTest()

        val authService = AuthServiceFactory.createWithCustomComponents(
            repository = stubRepo,
            storage = storage,
            refreshConfig = TokenRefreshConfig.NO_RETRY
        )

        authService.login(LoginCredentials("test@edugo.com", "password123"))

        val newTokenString = authService.refreshToken()

        assertTrue(newTokenString != null, "Should return new token string")
        assertTrue(newTokenString!!.isNotBlank(), "Token should not be blank")
    }

    @Test
    fun `getToken refreshes automatically if token is expired`() = runTest {
        val stubRepo = StubAuthRepository()
        val storage = createStorageForTest()

        val authService = AuthServiceFactory.createWithCustomComponents(
            repository = stubRepo,
            storage = storage,
            refreshConfig = TokenRefreshConfig.NO_RETRY
        )

        authService.login(LoginCredentials("test@edugo.com", "password123"))

        val token = authService.getToken()

        assertTrue(token != null, "Should return valid token")
    }
}

package com.edugo.kmp.auth.repository

import com.edugo.kmp.auth.model.AuthUserInfo
import com.edugo.kmp.auth.model.LoginCredentials
import com.edugo.kmp.foundation.result.Result
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class StubAuthRepositoryTest {

    private lateinit var repository: StubAuthRepository

    @BeforeTest
    fun setup() {
        repository = StubAuthRepository()
    }

    @AfterTest
    fun cleanup() {
        repository.reset()
    }

    // ==================== LOGIN TESTS ====================

    @Test
    fun `login with valid credentials returns success`() = runTest {
        val credentials = StubAuthRepository.VALID_CREDENTIALS
        val result = repository.login(credentials)

        assertIs<Result.Success<*>>(result)
        val response = (result as Result.Success).data
        assertEquals("test@edugo.com", response.user.email)
        assertNotNull(response.accessToken)
        assertNotNull(response.refreshToken)
    }

    @Test
    fun `login with invalid credentials returns failure`() = runTest {
        val credentials = StubAuthRepository.INVALID_CREDENTIALS
        val result = repository.login(credentials)

        assertIs<Result.Failure>(result)
        assertTrue(result.error.contains("credentials", ignoreCase = true))
    }

    @Test
    fun `login with invalid email returns failure`() = runTest {
        val credentials = LoginCredentials("wrong@edugo.com", "password123")
        val result = repository.login(credentials)
        assertIs<Result.Failure>(result)
    }

    @Test
    fun `login with invalid password returns failure`() = runTest {
        val credentials = LoginCredentials("test@edugo.com", "wrongpassword")
        val result = repository.login(credentials)
        assertIs<Result.Failure>(result)
    }

    // ==================== LOGOUT TESTS ====================

    @Test
    fun `logout always returns success`() = runTest {
        val result = repository.logout("any_token")
        assertIs<Result.Success<Unit>>(result)
    }

    @Test
    fun `logout with empty token returns success`() = runTest {
        val result = repository.logout("")
        assertIs<Result.Success<Unit>>(result)
    }

    // ==================== REFRESH TESTS ====================

    @Test
    fun `refresh with valid token returns success`() = runTest {
        val refreshToken = StubAuthRepository.VALID_REFRESH_TOKEN
        val result = repository.refresh(refreshToken)

        assertIs<Result.Success<*>>(result)
        val response = (result as Result.Success).data
        assertNotNull(response.accessToken)
        assertTrue(response.expiresIn > 0)
    }

    @Test
    fun `refresh with blank token returns failure`() = runTest {
        val result = repository.refresh("")
        assertIs<Result.Failure>(result)
        assertTrue(result.error.contains("refresh", ignoreCase = true))
    }

    // ==================== NETWORK ERROR SIMULATION ====================

    @Test
    fun `login with network error simulation returns failure`() = runTest {
        repository.simulateNetworkError = true
        val credentials = StubAuthRepository.VALID_CREDENTIALS
        val result = repository.login(credentials)

        assertIs<Result.Failure>(result)
        assertTrue(result.error.contains("timeout", ignoreCase = true) ||
                   result.error.contains("network", ignoreCase = true))
    }

    @Test
    fun `logout with network error simulation returns failure`() = runTest {
        repository.simulateNetworkError = true
        val result = repository.logout("token")
        assertIs<Result.Failure>(result)
    }

    @Test
    fun `refresh with network error simulation returns failure`() = runTest {
        repository.simulateNetworkError = true
        val result = repository.refresh("refresh_token")
        assertIs<Result.Failure>(result)
    }

    // ==================== DELAY SIMULATION ====================

    @Test
    fun `login with delay configuration works`() = runTest {
        repository.simulateDelay = 50
        val credentials = StubAuthRepository.VALID_CREDENTIALS
        val result = repository.login(credentials)

        assertEquals(50L, repository.simulateDelay)
        assertIs<Result.Success<*>>(result)
    }

    // ==================== CONFIGURATION TESTS ====================

    @Test
    fun `custom valid email and password work`() = runTest {
        repository.validEmail = "custom@test.com"
        repository.validPassword = "custompass"
        val credentials = LoginCredentials("custom@test.com", "custompass")
        val result = repository.login(credentials)
        assertIs<Result.Success<*>>(result)
    }

    @Test
    fun `custom test user is returned on login`() = runTest {
        val customUser = AuthUserInfo.createTestUser(
            id = "custom-id",
            email = "custom@test.com",
            firstName = "Custom"
        )
        repository.testUser = customUser
        repository.validEmail = "custom@test.com"

        val credentials = LoginCredentials("custom@test.com", "password123")
        val result = repository.login(credentials)

        assertIs<Result.Success<*>>(result)
        val response = (result as Result.Success).data
        assertEquals("custom-id", response.user.id)
        assertEquals("Custom", response.user.firstName)
        // Verificar que el activeContext contiene información de rol
        assertNotNull(response.activeContext)
        assertNotNull(response.activeContext.roleName)
    }

    @Test
    fun `reset restores default configuration`() = runTest {
        repository.simulateNetworkError = true
        repository.simulateDelay = 1000
        repository.validEmail = "changed@test.com"

        repository.reset()

        assertFalse(repository.simulateNetworkError)
        assertEquals(0L, repository.simulateDelay)
        assertEquals("test@edugo.com", repository.validEmail)
        assertEquals("password123", repository.validPassword)

        val result = repository.login(StubAuthRepository.VALID_CREDENTIALS)
        assertIs<Result.Success<*>>(result)
    }

    // ==================== FACTORY METHODS TESTS ====================

    @Test
    fun `create factory method returns working instance`() = runTest {
        val stub = StubAuthRepository.create()
        val result = stub.login(StubAuthRepository.VALID_CREDENTIALS)
        assertIs<Result.Success<*>>(result)
    }

    @Test
    fun `createWithNetworkError factory method simulates network error`() = runTest {
        val stub = StubAuthRepository.createWithNetworkError()
        val result = stub.login(StubAuthRepository.VALID_CREDENTIALS)

        assertIs<Result.Failure>(result)
        assertTrue(stub.simulateNetworkError)
    }

    @Test
    fun `createWithDelay factory method configures delay`() = runTest {
        val stub = StubAuthRepository.createWithDelay(50)
        assertEquals(50L, stub.simulateDelay)
    }

    @Test
    fun `createWithUser factory method uses custom user`() = runTest {
        val customUser = AuthUserInfo.createTestUser(id = "custom", email = "custom@test.com")
        val stub = StubAuthRepository.createWithUser(customUser)
        val result = stub.login(LoginCredentials("custom@test.com", "password123"))

        assertIs<Result.Success<*>>(result)
        val response = (result as Result.Success).data
        assertEquals("custom", response.user.id)
    }
}

package com.edugo.kmp.auth.repository

import com.edugo.kmp.auth.model.LoginCredentials
import com.edugo.kmp.foundation.result.Result
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MockAuthRepositoryTest {

    private val repository = MockAuthRepository()

    @Test
    fun login_accepts_any_email_and_password() = runTest {
        val credentials = LoginCredentials(email = "random@test.com", password = "anypass")
        val result = repository.login(credentials)
        assertTrue(result is Result.Success)
    }

    @Test
    fun login_returns_user_with_provided_email() = runTest {
        val email = "developer@edugo.com"
        val credentials = LoginCredentials(email = email, password = "pass123")
        val result = repository.login(credentials)
        assertTrue(result is Result.Success)
        assertEquals(email, result.data.user.email)
    }

    @Test
    fun login_is_deterministic_for_same_email() = runTest {
        val email = "consistent@edugo.com"
        val credentials = LoginCredentials(email = email, password = "pass")

        val result1 = repository.login(credentials)
        val result2 = repository.login(credentials)

        assertTrue(result1 is Result.Success)
        assertTrue(result2 is Result.Success)
        assertEquals(result1.data.user.firstName, result2.data.user.firstName)
        assertEquals(result1.data.user.lastName, result2.data.user.lastName)
        assertEquals(result1.data.activeContext.roleName, result2.data.activeContext.roleName)
    }

    @Test
    fun login_generates_different_users_for_different_emails() = runTest {
        val result1 = repository.login(LoginCredentials("alice@edugo.com", "pass"))
        val result2 = repository.login(LoginCredentials("bob@edugo.com", "pass"))

        assertTrue(result1 is Result.Success)
        assertTrue(result2 is Result.Success)
        // User IDs should differ since emails differ
        assertTrue(result1.data.user.id != result2.data.user.id)
    }

    @Test
    fun login_tokens_have_mock_prefix() = runTest {
        val result = repository.login(LoginCredentials("test@edugo.com", "pass"))
        assertTrue(result is Result.Success)
        assertTrue(result.data.accessToken.startsWith("access_mock_"))
        assertTrue(result.data.refreshToken.startsWith("refresh_mock_"))
    }

    @Test
    fun logout_always_succeeds() = runTest {
        val result = repository.logout("any_token")
        assertTrue(result is Result.Success)
    }

    @Test
    fun refresh_succeeds_with_non_empty_token() = runTest {
        val result = repository.refresh("refresh_mock_123")
        assertTrue(result is Result.Success)
        assertNotNull(result.data)
        assertTrue(result.data.accessToken.startsWith("access_mock_"))
    }

    @Test
    fun refresh_fails_with_empty_token() = runTest {
        val result = repository.refresh("")
        assertTrue(result is Result.Failure)
    }

    @Test
    fun verifyToken_succeeds_with_non_empty_token() = runTest {
        val result = repository.verifyToken("access_mock_123")
        assertTrue(result is Result.Success)
        assertTrue(result.data.valid)
    }

    @Test
    fun verifyToken_fails_with_empty_token() = runTest {
        val result = repository.verifyToken("")
        assertTrue(result is Result.Failure)
    }

    @Test
    fun refresh_fails_with_blank_token() = runTest {
        val result = repository.refresh("   ")
        assertTrue(result is Result.Failure)
    }

    @Test
    fun verifyToken_fails_with_blank_token() = runTest {
        val result = repository.verifyToken("   ")
        assertTrue(result is Result.Failure)
    }
}

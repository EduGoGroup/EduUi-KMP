package com.edugo.kmp.auth.service

import com.edugo.kmp.auth.model.LoginCredentials
import com.edugo.kmp.auth.model.LogoutResult
import com.edugo.kmp.auth.model.isPartial
import com.edugo.kmp.auth.model.isSuccess
import com.edugo.kmp.auth.model.localCleared
import com.edugo.kmp.auth.model.wasAlreadyLoggedOut
import com.edugo.kmp.foundation.result.Result
import com.edugo.kmp.storage.EduGoStorage
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LogoutTest {

    private lateinit var authService: AuthService

    private val validCredentials = LoginCredentials(
        email = "test@edugo.com",
        password = "password123"
    )

    @BeforeTest
    fun setup() {
        authService = AuthServiceFactory.createForTesting(
            storage = EduGoStorage.withSettings(MapSettings())
        )
    }

    @Test
    fun `logoutWithDetails exitoso limpia todo y retorna Success`() = runTest {
        authService.login(validCredentials)
        assertTrue(authService.isAuthenticated())

        val result = authService.logoutWithDetails()

        assertEquals(LogoutResult.Success, result)
        assertFalse(authService.isAuthenticated())
        assertEquals(AuthState.Unauthenticated, authService.authState.value)
    }

    @Test
    fun `logoutWithDetails idempotente retorna AlreadyLoggedOut`() = runTest {
        assertFalse(authService.isAuthenticated())

        val result = authService.logoutWithDetails()

        assertEquals(LogoutResult.AlreadyLoggedOut, result)
        assertFalse(authService.isAuthenticated())
    }

    @Test
    fun `logoutWithDetails despues de login segundo logout es idempotente`() = runTest {
        authService.login(validCredentials)
        assertTrue(authService.isAuthenticated())

        val result1 = authService.logoutWithDetails()
        val result2 = authService.logoutWithDetails()

        assertEquals(LogoutResult.Success, result1)
        assertEquals(LogoutResult.AlreadyLoggedOut, result2)
    }

    @Test
    fun `logout con metodo legacy sigue funcionando`() = runTest {
        authService.login(validCredentials)
        assertTrue(authService.isAuthenticated())

        val result = authService.logout()

        assertTrue(result is Result.Success)
        assertFalse(authService.isAuthenticated())
    }

    @Test
    fun `extension properties de LogoutResult funcionan correctamente`() {
        val success = LogoutResult.Success
        assertTrue(success.isSuccess)
        assertTrue(success.localCleared)
        assertFalse(success.isPartial)
        assertFalse(success.wasAlreadyLoggedOut)

        val partial = LogoutResult.PartialSuccess("Network error")
        assertFalse(partial.isSuccess)
        assertTrue(partial.localCleared)
        assertTrue(partial.isPartial)
        assertFalse(partial.wasAlreadyLoggedOut)

        val alreadyOut = LogoutResult.AlreadyLoggedOut
        assertFalse(alreadyOut.isSuccess)
        assertFalse(alreadyOut.localCleared)
        assertFalse(alreadyOut.isPartial)
        assertTrue(alreadyOut.wasAlreadyLoggedOut)
    }

    @Test
    fun `multiples logouts concurrentes son seguros`() = runTest {
        authService.login(validCredentials)
        assertTrue(authService.isAuthenticated())

        val result1 = authService.logoutWithDetails()
        val result2 = authService.logoutWithDetails()
        val result3 = authService.logoutWithDetails()

        assertEquals(LogoutResult.Success, result1)
        assertEquals(LogoutResult.AlreadyLoggedOut, result2)
        assertEquals(LogoutResult.AlreadyLoggedOut, result3)
        assertFalse(authService.isAuthenticated())
    }

    @Test
    fun `logoutWithDetails con forceLocal=true limpia local`() = runTest {
        authService.login(validCredentials)

        val result = authService.logoutWithDetails(forceLocal = true)

        assertTrue(result is LogoutResult.Success || result is LogoutResult.PartialSuccess)
        assertFalse(authService.isAuthenticated())
    }

    @Test
    fun `logoutWithDetails con forceLocal=false tambien limpia si backend OK`() = runTest {
        authService.login(validCredentials)

        val result = authService.logoutWithDetails(forceLocal = false)

        assertEquals(LogoutResult.Success, result)
        assertFalse(authService.isAuthenticated())
    }
}

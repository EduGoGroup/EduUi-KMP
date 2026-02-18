package com.edugo.kmp.auth.service

import com.edugo.kmp.auth.model.AuthToken
import com.edugo.kmp.auth.model.AuthUserInfo
import com.edugo.kmp.auth.model.UserContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthStateTest {

    private val testUser = AuthUserInfo.createTestUser()
    private val testToken = AuthToken.createTestToken()
    private val testContext = UserContext.createTestContext()

    @Test
    fun testUnauthenticatedState() {
        val state = AuthState.Unauthenticated

        assertFalse(state.isAuthenticated)
        assertTrue(state.isUnauthenticated)
        assertFalse(state.isLoading)
        assertNull(state.currentUser)
        assertNull(state.currentToken)
    }

    @Test
    fun testAuthenticatedState() {
        val state = AuthState.Authenticated(testUser, testToken, testContext)

        assertTrue(state.isAuthenticated)
        assertFalse(state.isUnauthenticated)
        assertFalse(state.isLoading)
        assertEquals(testUser, state.currentUser)
        assertEquals(testToken, state.currentToken)
    }

    @Test
    fun testLoadingState() {
        val state = AuthState.Loading

        assertFalse(state.isAuthenticated)
        assertFalse(state.isUnauthenticated)
        assertTrue(state.isLoading)
        assertNull(state.currentUser)
        assertNull(state.currentToken)
    }

    @Test
    fun testIfAuthenticatedExtensionExecutes() {
        val authenticatedState = AuthState.Authenticated(testUser, testToken, testContext)
        var called = false

        authenticatedState.ifAuthenticated { user, token, context ->
            called = true
            assertEquals(testUser, user)
            assertEquals(testToken, token)
            assertEquals(testContext, context)
        }

        assertTrue(called, "ifAuthenticated should execute for Authenticated state")
    }

    @Test
    fun testIfAuthenticatedExtensionDoesNotExecuteForUnauthenticated() {
        val unauthenticatedState = AuthState.Unauthenticated
        var called = false

        unauthenticatedState.ifAuthenticated { _, _, _ ->
            called = true
        }

        assertFalse(called, "ifAuthenticated should not execute for Unauthenticated state")
    }

    @Test
    fun testIfUnauthenticatedExtensionExecutes() {
        val unauthenticatedState = AuthState.Unauthenticated
        var called = false

        unauthenticatedState.ifUnauthenticated {
            called = true
        }

        assertTrue(called, "ifUnauthenticated should execute for Unauthenticated state")
    }

    @Test
    fun testIfUnauthenticatedExtensionDoesNotExecuteForAuthenticated() {
        val authenticatedState = AuthState.Authenticated(testUser, testToken, testContext)
        var called = false

        authenticatedState.ifUnauthenticated {
            called = true
        }

        assertFalse(called, "ifUnauthenticated should not execute for Authenticated state")
    }

    @Test
    fun testFoldPatternMatchingForAuthenticated() {
        val authenticatedState = AuthState.Authenticated(testUser, testToken, testContext)

        val result = authenticatedState.fold(
            onAuthenticated = { user, _, _ -> user.email },
            onUnauthenticated = { "not authenticated" },
            onLoading = { "loading" }
        )

        assertEquals(testUser.email, result)
    }

    @Test
    fun testFoldPatternMatchingForUnauthenticated() {
        val unauthenticatedState = AuthState.Unauthenticated

        val result = unauthenticatedState.fold(
            onAuthenticated = { _, _, _ -> "authenticated" },
            onUnauthenticated = { "not authenticated" },
            onLoading = { "loading" }
        )

        assertEquals("not authenticated", result)
    }

    @Test
    fun testFoldPatternMatchingForLoading() {
        val loadingState = AuthState.Loading

        val result = loadingState.fold(
            onAuthenticated = { _, _, _ -> "authenticated" },
            onUnauthenticated = { "not authenticated" },
            onLoading = { "loading" }
        )

        assertEquals("loading", result)
    }

    @Test
    fun testAuthenticatedStateCopy() {
        val original = AuthState.Authenticated(testUser, testToken, testContext)
        val newToken = AuthToken.createTestToken(durationSeconds = 7200)

        val copied = original.copy(token = newToken)

        assertEquals(testUser, copied.user)
        assertEquals(newToken, copied.token)
        assertTrue(copied.isAuthenticated)
    }

    @Test
    fun testCurrentUserIsNullForLoading() {
        val state = AuthState.Loading
        assertNull(state.currentUser)
    }

    @Test
    fun testCurrentTokenIsNullForLoading() {
        val state = AuthState.Loading
        assertNull(state.currentToken)
    }
}

package com.edugo.kmp.auth.model

import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class LoginResponseTest {

    // ==================== TESTS DE DESERIALIZACIÓN ====================

    @Test
    fun `deserialization from backend JSON works correctly`() {
        val json = """
            {
              "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test",
              "expires_in": 3600,
              "refresh_token": "refresh_token_123",
              "token_type": "Bearer",
              "user": {
                "id": "user-123",
                "email": "test@edugo.com",
                "first_name": "Test",
                "last_name": "User",
                "full_name": "Test User",
                "school_id": "school-456"
              },
              "active_context": {
                "role_id": "role-1",
                "role_name": "student",
                "school_id": "school-456",
                "school_name": "Test School",
                "permissions": ["materials.read", "progress.write"]
              }
            }
        """.trimIndent()

        val response = kotlinx.serialization.json.Json.decodeFromString(LoginResponse.serializer(), json)

        assertEquals("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test", response.accessToken)
        assertEquals(3600, response.expiresIn)
        assertEquals("refresh_token_123", response.refreshToken)
        assertEquals("Bearer", response.tokenType)
        assertEquals("user-123", response.user.id)
        assertEquals("test@edugo.com", response.user.email)
        assertEquals("student", response.activeContext.roleName)
    }

    @Test
    fun `deserialization handles user without school_id`() {
        val json = """
            {
              "access_token": "token123",
              "expires_in": 3600,
              "refresh_token": "refresh123",
              "token_type": "Bearer",
              "user": {
                "id": "user-123",
                "email": "test@edugo.com",
                "first_name": "Test",
                "last_name": "User",
                "full_name": "Test User"
              },
              "active_context": {
                "role_id": "role-2",
                "role_name": "admin",
                "permissions": ["materials.read", "materials.write", "users.manage"]
              }
            }
        """.trimIndent()

        val response = kotlinx.serialization.json.Json.decodeFromString(LoginResponse.serializer(), json)
        assertEquals(null, response.user.schoolId)
        assertEquals(null, response.activeContext.schoolId)
    }

    // ==================== TESTS DE CONVERSIÓN A AUTHTOKEN ====================

    @Test
    fun `toAuthToken creates valid AuthToken`() {
        val now = Clock.System.now()
        val response = LoginResponse.createTestResponse(
            accessToken = "test_token",
            expiresIn = 3600,
            refreshToken = "refresh_token"
        )

        val authToken = response.toAuthToken()

        assertEquals("test_token", authToken.token)
        assertEquals("refresh_token", authToken.refreshToken)
        assertTrue(authToken.expiresAt > now)

        val expectedExpiration = now + 3600.seconds
        val timeDifference = (authToken.expiresAt - expectedExpiration).inWholeSeconds
        assertTrue(timeDifference < 2, "expiresAt should be within 2 seconds of expected")
    }

    @Test
    fun `toAuthToken calculates expiration correctly for different durations`() {
        val testCases = listOf(1800, 3600, 7200, 86400)

        testCases.forEach { expiresIn ->
            val now = Clock.System.now()
            val response = LoginResponse.createTestResponse(expiresIn = expiresIn)
            val authToken = response.toAuthToken()

            val expectedExpiration = now + expiresIn.seconds
            val timeDifference = (authToken.expiresAt - expectedExpiration).inWholeSeconds
            assertTrue(
                timeDifference < 2,
                "expiresAt should be within 2 seconds of expected for $expiresIn seconds"
            )
        }
    }

    @Test
    fun `toAuthToken creates non-expired token for positive expiresIn`() {
        val response = LoginResponse.createTestResponse(expiresIn = 3600)
        val authToken = response.toAuthToken()

        assertFalse(authToken.isExpired(), "Token should not be expired immediately after creation")
        assertTrue(authToken.isValid(), "Token should be valid immediately after creation")
    }

    // ==================== TESTS DE SERIALIZACIÓN ROUND-TRIP ====================

    @Test
    fun `serialization round-trip preserves all data`() {
        val original = LoginResponse(
            accessToken = "test_token_123",
            expiresIn = 3600,
            refreshToken = "refresh_token_123",
            tokenType = "Bearer",
            user = AuthUserInfo(
                id = "user-123",
                email = "test@edugo.com",
                firstName = "Test",
                lastName = "User",
                fullName = "Test User",
                schoolId = "school-456"
            ),
            activeContext = UserContext(
                roleId = "role-1",
                roleName = "student",
                schoolId = "school-456",
                schoolName = "Test School",
                permissions = listOf("materials.read", "progress.write")
            )
        )

        val json = kotlinx.serialization.json.Json.encodeToString(LoginResponse.serializer(), original)
        val deserialized = kotlinx.serialization.json.Json.decodeFromString(LoginResponse.serializer(), json)

        assertEquals(original.accessToken, deserialized.accessToken)
        assertEquals(original.expiresIn, deserialized.expiresIn)
        assertEquals(original.refreshToken, deserialized.refreshToken)
        assertEquals(original.tokenType, deserialized.tokenType)
        assertEquals(original.user.id, deserialized.user.id)
        assertEquals(original.user.email, deserialized.user.email)
        assertEquals(original.activeContext.roleName, deserialized.activeContext.roleName)
        assertEquals(original.activeContext.permissions, deserialized.activeContext.permissions)
    }

    // ==================== TESTS DE MÉTODOS UTILITARIOS ====================

    @Test
    fun `isBearerToken returns true for Bearer token`() {
        val response = LoginResponse.createTestResponse()
        assertTrue(response.isBearerToken())
    }

    @Test
    fun `isBearerToken is case insensitive`() {
        val testCases = listOf("Bearer", "bearer", "BEARER", "BeArEr")
        testCases.forEach { tokenType ->
            val response = LoginResponse.createTestResponse(
                accessToken = "token",
                expiresIn = 3600,
                refreshToken = "refresh"
            ).copy(tokenType = tokenType)
            assertTrue(response.isBearerToken(), "isBearerToken should return true for '$tokenType'")
        }
    }

    @Test
    fun `isBearerToken returns false for non-Bearer token`() {
        val response = LoginResponse.createTestResponse().copy(tokenType = "Basic")
        assertFalse(response.isBearerToken())
    }

    @Test
    fun `getAuthorizationHeader returns correctly formatted header`() {
        val response = LoginResponse.createTestResponse(accessToken = "abc123xyz")
        val header = response.getAuthorizationHeader()
        assertEquals("Bearer abc123xyz", header)
    }

    @Test
    fun `calculateExpirationTime returns future instant`() {
        val now = Clock.System.now()
        val response = LoginResponse.createTestResponse(expiresIn = 3600)
        val expirationTime = response.calculateExpirationTime()
        assertTrue(expirationTime > now, "Expiration time should be in the future")
    }

    @Test
    fun `calculateExpirationTime matches expiresIn duration`() {
        val now = Clock.System.now()
        val expiresInSeconds = 1800
        val response = LoginResponse.createTestResponse(expiresIn = expiresInSeconds)
        val expirationTime = response.calculateExpirationTime()

        val expectedExpiration = now + expiresInSeconds.seconds
        val timeDifference = (expirationTime - expectedExpiration).inWholeSeconds
        assertTrue(timeDifference < 2, "Expiration time should be within 2 seconds of expected")
    }

    // ==================== TESTS DE LOGGING ====================

    @Test
    fun `toLogString does not expose full token`() {
        val response = LoginResponse.createTestResponse(
            accessToken = "very_long_secret_access_token_that_should_not_be_logged"
        )
        val logString = response.toLogString()
        assertFalse(logString.contains("very_long_secret_access_token_that_should_not_be_logged"), "Log string should not contain full token")
        assertTrue(logString.contains("..."), "Log string should contain truncation indicator")
    }

    @Test
    fun `toLogString includes useful debug information`() {
        val response = LoginResponse.createTestResponse()
        val logString = response.toLogString()
        assertTrue(logString.contains("Bearer"), "Should include token type")
        assertTrue(logString.contains("3600"), "Should include expiresIn")
        assertTrue(logString.contains("test-user-123"), "Should include user ID")
        assertTrue(logString.contains("student"), "Should include user role")
    }

    @Test
    fun `toLogString handles short tokens`() {
        val response = LoginResponse.createTestResponse(accessToken = "short")
        val logString = response.toLogString()
        assertTrue(logString.contains("***"), "Short tokens should be fully masked")
        assertFalse(logString.contains("short"), "Short token should not be exposed")
    }

    // ==================== TESTS DE FACTORY METHOD ====================

    @Test
    fun `createTestResponse returns valid response`() {
        val response = LoginResponse.createTestResponse()
        assertNotNull(response.accessToken)
        assertTrue(response.accessToken.startsWith("test_access_token_"))
        assertEquals(3600, response.expiresIn)
        assertNotNull(response.refreshToken)
        assertTrue(response.refreshToken.startsWith("test_refresh_token_"))
        assertEquals("Bearer", response.tokenType)
        assertEquals("test-user-123", response.user.id)
    }

    @Test
    fun `createTestResponse accepts custom parameters`() {
        val customUser = AuthUserInfo.createTestUser(id = "custom-id", email = "custom@test.com")
        val response = LoginResponse.createTestResponse(
            accessToken = "custom_token",
            expiresIn = 7200,
            refreshToken = "custom_refresh",
            user = customUser
        )
        assertEquals("custom_token", response.accessToken)
        assertEquals(7200, response.expiresIn)
        assertEquals("custom_refresh", response.refreshToken)
        assertEquals("custom-id", response.user.id)
        assertEquals("custom@test.com", response.user.email)
    }

    @Test
    fun `createTestResponse with different parameters generates different tokens`() {
        val response1 = LoginResponse.createTestResponse(accessToken = "token_a", refreshToken = "refresh_a")
        val response2 = LoginResponse.createTestResponse(accessToken = "token_b", refreshToken = "refresh_b")
        assertTrue(
            response1.accessToken != response2.accessToken,
            "Access tokens should be different"
        )
        assertTrue(
            response1.refreshToken != response2.refreshToken,
            "Refresh tokens should be different"
        )
    }
}

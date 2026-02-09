package com.edugo.kmp.auth.model

import com.edugo.kmp.foundation.result.Result
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class LoginCredentialsTest {

    // ==================== TESTS DE VALIDACIÓN DE EMAIL ====================

    @Test
    fun `validate returns success for valid email and password`() {
        val credentials = LoginCredentials(
            email = "user@edugo.com",
            password = "password123"
        )
        val result = credentials.validate()
        assertIs<Result.Success<Unit>>(result)
    }

    @Test
    fun `validate fails when email is blank`() {
        val credentials = LoginCredentials(email = "", password = "password123")
        val result = credentials.validate()
        assertIs<Result.Failure>(result)
        assertEquals("Email cannot be blank", result.error)
    }

    @Test
    fun `validate fails when email has no at symbol`() {
        val credentials = LoginCredentials(email = "notanemail.com", password = "password123")
        val result = credentials.validate()
        assertIs<Result.Failure>(result)
        assertEquals("Email must be a valid email address", result.error)
    }

    @Test
    fun `validate fails when email has multiple at symbols`() {
        val credentials = LoginCredentials(email = "user@@edugo.com", password = "password123")
        val result = credentials.validate()
        assertIs<Result.Failure>(result)
        assertEquals("Email must be a valid email address", result.error)
    }

    @Test
    fun `validate fails when email has no local part`() {
        val credentials = LoginCredentials(email = "@edugo.com", password = "password123")
        val result = credentials.validate()
        assertIs<Result.Failure>(result)
        assertEquals("Email must be a valid email address", result.error)
    }

    @Test
    fun `validate fails when email has no domain`() {
        val credentials = LoginCredentials(email = "user@", password = "password123")
        val result = credentials.validate()
        assertIs<Result.Failure>(result)
        assertEquals("Email must be a valid email address", result.error)
    }

    @Test
    fun `validate fails when email domain has no dot`() {
        val credentials = LoginCredentials(email = "user@edugo", password = "password123")
        val result = credentials.validate()
        assertIs<Result.Failure>(result)
        assertEquals("Email must be a valid email address", result.error)
    }

    @Test
    fun `validate succeeds with valid email formats`() {
        val validEmails = listOf(
            "user@edugo.com",
            "test.user@edugo.com",
            "test+tag@edugo.com",
            "user123@subdomain.edugo.com",
            "a@b.co"
        )
        validEmails.forEach { email ->
            val credentials = LoginCredentials(email = email, password = "password123")
            val result = credentials.validate()
            assertIs<Result.Success<Unit>>(result, "Email $email should be valid")
        }
    }

    // ==================== TESTS DE VALIDACIÓN DE PASSWORD ====================

    @Test
    fun `validate fails when password is blank`() {
        val credentials = LoginCredentials(email = "user@edugo.com", password = "")
        val result = credentials.validate()
        assertIs<Result.Failure>(result)
        assertEquals("Password cannot be blank", result.error)
    }

    @Test
    fun `validate fails when password is too short`() {
        val credentials = LoginCredentials(email = "user@edugo.com", password = "short")
        val result = credentials.validate()
        assertIs<Result.Failure>(result)
        assertEquals("Password must be at least 8 characters long", result.error)
    }

    @Test
    fun `validate succeeds when password has exactly minimum length`() {
        val credentials = LoginCredentials(email = "user@edugo.com", password = "12345678")
        val result = credentials.validate()
        assertIs<Result.Success<Unit>>(result)
    }

    @Test
    fun `validate succeeds when password is longer than minimum`() {
        val credentials = LoginCredentials(email = "user@edugo.com", password = "verylongpassword123")
        val result = credentials.validate()
        assertIs<Result.Success<Unit>>(result)
    }

    // ==================== TESTS DE SERIALIZACIÓN ====================

    @Test
    fun `serialization preserves email and password fields`() {
        val credentials = LoginCredentials(email = "user@edugo.com", password = "password123")
        val json = kotlinx.serialization.json.Json.encodeToString(LoginCredentials.serializer(), credentials)
        val deserialized = kotlinx.serialization.json.Json.decodeFromString(LoginCredentials.serializer(), json)
        assertEquals(credentials.email, deserialized.email)
        assertEquals(credentials.password, deserialized.password)
    }

    @Test
    fun `serialization uses expected field names`() {
        val credentials = LoginCredentials(email = "user@edugo.com", password = "password123")
        val json = kotlinx.serialization.json.Json.encodeToString(LoginCredentials.serializer(), credentials)
        assertTrue(json.contains("\"email\""))
        assertTrue(json.contains("\"password\""))
        assertTrue(json.contains("user@edugo.com"))
        assertTrue(json.contains("password123"))
    }

    @Test
    fun `deserialization from JSON works correctly`() {
        val json = """{"email":"test@edugo.com","password":"testpass123"}"""
        val credentials = kotlinx.serialization.json.Json.decodeFromString(LoginCredentials.serializer(), json)
        assertEquals("test@edugo.com", credentials.email)
        assertEquals("testpass123", credentials.password)
    }

    // ==================== TESTS DE UTILIDADES ====================

    @Test
    fun `toLogString does not expose password`() {
        val credentials = LoginCredentials(email = "user@edugo.com", password = "supersecretpassword")
        val logString = credentials.toLogString()
        assertTrue(logString.contains("user@edugo.com"))
        assertFalse(logString.contains("supersecretpassword"))
        assertTrue(logString.contains("***"))
    }

    @Test
    fun `createTestCredentials returns valid credentials`() {
        val credentials = LoginCredentials.createTestCredentials()
        assertIs<Result.Success<Unit>>(credentials.validate())
        assertEquals("test@edugo.com", credentials.email)
        assertEquals("password123", credentials.password)
    }

    @Test
    fun `createTestCredentials accepts custom parameters`() {
        val credentials = LoginCredentials.createTestCredentials(email = "custom@test.com", password = "custompass")
        assertEquals("custom@test.com", credentials.email)
        assertEquals("custompass", credentials.password)
    }

    @Test
    fun `MIN_PASSWORD_LENGTH constant has expected value`() {
        assertEquals(8, LoginCredentials.MIN_PASSWORD_LENGTH)
    }

    // ==================== TESTS DE CASOS EDGE ====================

    @Test
    fun `validate trims whitespace from email before validation`() {
        val credentials = LoginCredentials(email = "  user@edugo.com  ", password = "password123")
        val result = credentials.validate()
        assertIs<Result.Success<Unit>>(result)
    }

    @Test
    fun `validate handles email with only whitespace`() {
        val credentials = LoginCredentials(email = "   ", password = "password123")
        val result = credentials.validate()
        assertIs<Result.Failure>(result)
        assertEquals("Email cannot be blank", result.error)
    }

    @Test
    fun `validate handles password with only whitespace`() {
        val credentials = LoginCredentials(email = "user@edugo.com", password = "        ")
        val result = credentials.validate()
        assertIs<Result.Failure>(result)
        assertEquals("Password cannot be blank", result.error)
    }
}

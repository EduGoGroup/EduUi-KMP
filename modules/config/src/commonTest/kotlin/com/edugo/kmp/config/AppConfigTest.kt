package com.edugo.kmp.config

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests para AppConfig y AppConfigImpl.
 */
class AppConfigTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun appConfigImpl_serializes_and_deserializes() {
        val config = AppConfigImpl(
            environmentName = "DEV",
            apiUrl = "http://localhost",
            apiPort = 8080,
            webPort = 3000,
            timeout = 30000L,
            debugMode = true
        )

        val jsonStr = json.encodeToString(AppConfigImpl.serializer(), config)
        val deserialized = json.decodeFromString(AppConfigImpl.serializer(), jsonStr)

        assertEquals(config, deserialized)
    }

    @Test
    fun appConfigImpl_environment_maps_from_name() {
        val config = AppConfigImpl(
            environmentName = "PROD",
            apiUrl = "https://api.example.com",
            apiPort = 443,
            webPort = 80,
            timeout = 60000L,
            debugMode = false
        )

        assertEquals(Environment.PROD, config.environment)
    }

    @Test
    fun appConfigImpl_environment_defaults_to_dev_for_unknown() {
        val config = AppConfigImpl(
            environmentName = "UNKNOWN",
            apiUrl = "http://localhost",
            apiPort = 8080,
            webPort = 3000,
            timeout = 30000L,
            debugMode = true
        )

        assertEquals(Environment.DEV, config.environment)
    }

    @Test
    fun getFullApiUrl_concatenates_correctly() {
        val config = AppConfigImpl(
            environmentName = "DEV",
            apiUrl = "http://localhost",
            apiPort = 8080,
            webPort = 3000,
            timeout = 30000L,
            debugMode = true
        )

        assertEquals("http://localhost:8080", config.getFullApiUrl())
    }

    @Test
    fun getFullApiUrl_works_with_https_and_443() {
        val config = AppConfigImpl(
            environmentName = "PROD",
            apiUrl = "https://api.example.com",
            apiPort = 443,
            webPort = 80,
            timeout = 60000L,
            debugMode = false
        )

        assertEquals("https://api.example.com:443", config.getFullApiUrl())
    }

    @Test
    fun appConfigImpl_data_class_equality() {
        val config1 = AppConfigImpl("DEV", "http://localhost", 8080, 3000, 30000L, true)
        val config2 = AppConfigImpl("DEV", "http://localhost", 8080, 3000, 30000L, true)

        assertEquals(config1, config2)
        assertEquals(config1.hashCode(), config2.hashCode())
    }

    @Test
    fun debugMode_is_true_for_dev() {
        val config = AppConfigImpl("DEV", "http://localhost", 8080, 3000, 30000L, true)
        assertTrue(config.debugMode)
    }

    @Test
    fun debugMode_is_false_for_prod() {
        val config = AppConfigImpl("PROD", "https://api.example.com", 443, 80, 60000L, false)
        assertFalse(config.debugMode)
    }

    @Test
    fun mockMode_default_is_false() {
        val config = AppConfigImpl("DEV", "http://localhost", 8080, 3000, 30000L, true)
        assertFalse(config.mockMode)
    }

    @Test
    fun mockMode_can_be_enabled_for_dev() {
        val config = AppConfigImpl("DEV", "http://localhost", 8080, 3000, 30000L, true, mockModeValue = true)
        assertTrue(config.mockMode)
    }

    @Test
    fun mockMode_forced_false_in_prod() {
        val config = AppConfigImpl("PROD", "https://api.example.com", 443, 80, 60000L, false, mockModeValue = true)
        assertFalse(config.mockMode)
    }

    @Test
    fun mockMode_allowed_in_staging() {
        val config = AppConfigImpl("STAGING", "https://api-staging.example.com", 443, 8080, 60000L, true, mockModeValue = true)
        assertTrue(config.mockMode)
    }

    @Test
    fun mockMode_deserializes_from_json() {
        val jsonStr = """
            {
                "environmentName": "DEV",
                "apiUrl": "http://localhost",
                "apiPort": 8080,
                "webPort": 3000,
                "timeout": 30000,
                "debugMode": true,
                "mockMode": true
            }
        """
        val config = json.decodeFromString(AppConfigImpl.serializer(), jsonStr)
        assertTrue(config.mockMode)
    }

    @Test
    fun mockMode_missing_in_json_defaults_to_false() {
        val jsonStr = """
            {
                "environmentName": "DEV",
                "apiUrl": "http://localhost",
                "apiPort": 8080,
                "webPort": 3000,
                "timeout": 30000,
                "debugMode": true
            }
        """
        val config = json.decodeFromString(AppConfigImpl.serializer(), jsonStr)
        assertFalse(config.mockMode)
    }
}

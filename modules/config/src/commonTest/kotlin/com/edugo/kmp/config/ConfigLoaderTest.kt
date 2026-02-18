package com.edugo.kmp.config

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests para ConfigLoader y AppConfig.
 */
class ConfigLoaderTest {

    @Test
    fun load_dev_config_contains_correct_values() {
        val config = ConfigLoader.load(Environment.DEV)

        assertNotNull(config)
        assertEquals(Environment.DEV, config.environment)
        assertEquals("http://localhost:8081", config.adminApiBaseUrl)
        assertEquals("http://localhost:9091", config.mobileApiBaseUrl)
        assertEquals(8080, config.webPort)
        assertEquals(30000L, config.timeout)
        assertTrue(config.debugMode)
    }

    @Test
    fun load_staging_config_contains_correct_values() {
        val config = ConfigLoader.load(Environment.STAGING)

        assertNotNull(config)
        assertEquals(Environment.STAGING, config.environment)
        assertEquals("https://api-staging.example.com", config.adminApiBaseUrl)
        assertEquals("https://api-mobile-staging.example.com", config.mobileApiBaseUrl)
        assertEquals(8080, config.webPort)
        assertEquals(60000L, config.timeout)
        assertTrue(config.debugMode)
    }

    @Test
    fun load_prod_config_contains_correct_values() {
        val config = ConfigLoader.load(Environment.PROD)

        assertNotNull(config)
        assertEquals(Environment.PROD, config.environment)
        assertEquals("https://api.example.com", config.adminApiBaseUrl)
        assertEquals("https://api-mobile.example.com", config.mobileApiBaseUrl)
        assertEquals(80, config.webPort)
        assertEquals(60000L, config.timeout)
        assertFalse(config.debugMode)
    }

    @Test
    fun loadFromString_parses_json_correctly() {
        val jsonString = """
            {
              "environmentName": "DEV",
              "adminApiBaseUrl": "http://test.com:8081",
              "mobileApiBaseUrl": "http://test.com:9091",
              "webPort": 3000,
              "timeout": 15000,
              "debugMode": true
            }
        """.trimIndent()

        val config = ConfigLoader.loadFromString(jsonString)

        assertNotNull(config)
        assertEquals(Environment.DEV, config.environment)
        assertEquals("http://test.com:8081", config.adminApiBaseUrl)
        assertEquals("http://test.com:9091", config.mobileApiBaseUrl)
        assertEquals(3000, config.webPort)
        assertEquals(15000L, config.timeout)
        assertTrue(config.debugMode)
    }

    @Test
    fun loadFromString_ignores_unknown_keys() {
        val jsonString = """
            {
              "environmentName": "DEV",
              "adminApiBaseUrl": "http://test.com:8081",
              "mobileApiBaseUrl": "http://test.com:9091",
              "webPort": 3000,
              "timeout": 15000,
              "debugMode": true,
              "extraField": "should be ignored"
            }
        """.trimIndent()

        val config = ConfigLoader.loadFromString(jsonString)
        assertNotNull(config)
        assertEquals("http://test.com:8081", config.adminApiBaseUrl)
    }
}

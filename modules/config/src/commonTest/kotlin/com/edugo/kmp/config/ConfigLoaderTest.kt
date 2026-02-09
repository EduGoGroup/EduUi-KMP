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
        assertEquals("http://localhost", config.apiUrl)
        assertEquals(8080, config.apiPort)
        assertEquals(8080, config.webPort)
        assertEquals(30000L, config.timeout)
        assertTrue(config.debugMode)
    }

    @Test
    fun load_staging_config_contains_correct_values() {
        val config = ConfigLoader.load(Environment.STAGING)

        assertNotNull(config)
        assertEquals(Environment.STAGING, config.environment)
        assertEquals("https://api-staging.example.com", config.apiUrl)
        assertEquals(443, config.apiPort)
        assertEquals(8080, config.webPort)
        assertEquals(60000L, config.timeout)
        assertTrue(config.debugMode)
    }

    @Test
    fun load_prod_config_contains_correct_values() {
        val config = ConfigLoader.load(Environment.PROD)

        assertNotNull(config)
        assertEquals(Environment.PROD, config.environment)
        assertEquals("https://api.example.com", config.apiUrl)
        assertEquals(443, config.apiPort)
        assertEquals(80, config.webPort)
        assertEquals(60000L, config.timeout)
        assertFalse(config.debugMode)
    }

    @Test
    fun getFullApiUrl_concatenates_url_and_port() {
        val config = ConfigLoader.load(Environment.DEV)
        assertEquals("http://localhost:8080", config.getFullApiUrl())
    }

    @Test
    fun getFullApiUrl_works_for_prod() {
        val config = ConfigLoader.load(Environment.PROD)
        assertEquals("https://api.example.com:443", config.getFullApiUrl())
    }

    @Test
    fun loadFromString_parses_json_correctly() {
        val jsonString = """
            {
              "environmentName": "DEV",
              "apiUrl": "http://test.com",
              "apiPort": 9000,
              "webPort": 3000,
              "timeout": 15000,
              "debugMode": true
            }
        """.trimIndent()

        val config = ConfigLoader.loadFromString(jsonString)

        assertNotNull(config)
        assertEquals(Environment.DEV, config.environment)
        assertEquals("http://test.com", config.apiUrl)
        assertEquals(9000, config.apiPort)
        assertEquals(3000, config.webPort)
        assertEquals(15000L, config.timeout)
        assertTrue(config.debugMode)
    }

    @Test
    fun loadFromString_ignores_unknown_keys() {
        val jsonString = """
            {
              "environmentName": "DEV",
              "apiUrl": "http://test.com",
              "apiPort": 9000,
              "webPort": 3000,
              "timeout": 15000,
              "debugMode": true,
              "extraField": "should be ignored"
            }
        """.trimIndent()

        val config = ConfigLoader.loadFromString(jsonString)
        assertNotNull(config)
        assertEquals("http://test.com", config.apiUrl)
    }
}

package com.edugo.kmp.config

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConfigLoaderIntegrationTest {

    @AfterTest
    fun cleanup() {
        EnvironmentDetector.reset()
    }

    @Test
    fun loadDevConfigHasCorrectValues() {
        val config = ConfigLoader.load(Environment.DEV)

        assertEquals(Environment.DEV, config.environment)
        assertEquals("http://localhost:8081", config.adminApiBaseUrl)
        assertEquals("http://localhost:9091", config.mobileApiBaseUrl)
        assertEquals(8080, config.webPort)
        assertEquals(30000L, config.timeout)
        assertEquals(true, config.debugMode)
    }

    @Test
    fun loadStagingConfigHasCorrectValues() {
        val config = ConfigLoader.load(Environment.STAGING)

        assertEquals(Environment.STAGING, config.environment)
        assertEquals("https://api-staging.example.com", config.adminApiBaseUrl)
        assertEquals("https://api-mobile-staging.example.com", config.mobileApiBaseUrl)
        assertEquals(60000L, config.timeout)
        assertEquals(true, config.debugMode)
    }

    @Test
    fun loadProdConfigHasCorrectValues() {
        val config = ConfigLoader.load(Environment.PROD)

        assertEquals(Environment.PROD, config.environment)
        assertEquals("https://api.example.com", config.adminApiBaseUrl)
        assertEquals("https://api-mobile.example.com", config.mobileApiBaseUrl)
        assertEquals(80, config.webPort)
        assertEquals(60000L, config.timeout)
        assertEquals(false, config.debugMode)
    }

    @Test
    fun forceEnvironmentAffectsConfigLoading() {
        EnvironmentDetector.forceEnvironment(Environment.STAGING)
        val env = EnvironmentDetector.detect()
        val config = ConfigLoader.load(env)

        assertEquals(Environment.STAGING, config.environment)
        assertTrue(config.adminApiBaseUrl.contains("staging"))
    }

    @Test
    fun loadFromStringParsesValidJson() {
        val json = """
            {
              "environmentName": "DEV",
              "adminApiBaseUrl": "http://custom-server:8081",
              "mobileApiBaseUrl": "http://custom-server:9091",
              "webPort": 3000,
              "timeout": 5000,
              "debugMode": true
            }
        """.trimIndent()

        val config = ConfigLoader.loadFromString(json)

        assertEquals("http://custom-server:8081", config.adminApiBaseUrl)
        assertEquals("http://custom-server:9091", config.mobileApiBaseUrl)
    }
}

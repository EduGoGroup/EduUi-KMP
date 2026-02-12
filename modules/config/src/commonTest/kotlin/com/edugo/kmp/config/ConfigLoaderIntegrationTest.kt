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
        assertEquals("http://localhost", config.apiUrl)
        assertEquals(8080, config.apiPort)
        assertEquals(8080, config.webPort)
        assertEquals(30000L, config.timeout)
        assertEquals(true, config.debugMode)
    }

    @Test
    fun loadStagingConfigHasCorrectValues() {
        val config = ConfigLoader.load(Environment.STAGING)

        assertEquals(Environment.STAGING, config.environment)
        assertEquals("https://api-staging.example.com", config.apiUrl)
        assertEquals(443, config.apiPort)
        assertEquals(60000L, config.timeout)
        assertEquals(true, config.debugMode)
    }

    @Test
    fun loadProdConfigHasCorrectValues() {
        val config = ConfigLoader.load(Environment.PROD)

        assertEquals(Environment.PROD, config.environment)
        assertEquals("https://api.example.com", config.apiUrl)
        assertEquals(443, config.apiPort)
        assertEquals(80, config.webPort)
        assertEquals(60000L, config.timeout)
        assertEquals(false, config.debugMode)
    }

    @Test
    fun getFullApiUrlConstructsCorrectUrl() {
        val devConfig = ConfigLoader.load(Environment.DEV)
        assertEquals("http://localhost:8080", devConfig.getFullApiUrl())

        val prodConfig = ConfigLoader.load(Environment.PROD)
        assertEquals("https://api.example.com:443", prodConfig.getFullApiUrl())
    }

    @Test
    fun forceEnvironmentAffectsConfigLoading() {
        EnvironmentDetector.forceEnvironment(Environment.STAGING)
        val env = EnvironmentDetector.detect()
        val config = ConfigLoader.load(env)

        assertEquals(Environment.STAGING, config.environment)
        assertTrue(config.apiUrl.contains("staging"))
    }

    @Test
    fun loadFromStringParsesValidJson() {
        val json = """
            {
              "environmentName": "DEV",
              "apiUrl": "http://custom-server",
              "apiPort": 9090,
              "webPort": 3000,
              "timeout": 5000,
              "debugMode": true
            }
        """.trimIndent()

        val config = ConfigLoader.loadFromString(json)

        assertEquals("http://custom-server", config.apiUrl)
        assertEquals(9090, config.apiPort)
        assertEquals("http://custom-server:9090", config.getFullApiUrl())
    }
}

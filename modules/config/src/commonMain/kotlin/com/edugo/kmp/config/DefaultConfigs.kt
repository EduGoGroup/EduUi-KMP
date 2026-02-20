package com.edugo.kmp.config

/**
 * Fallback configurations used when external config files
 * cannot be loaded (e.g., during unit tests or when resources
 * are not bundled).
 *
 * Single source of truth for hardcoded config values.
 * These values MUST match the JSON files in resources/config/.
 */
internal object DefaultConfigs {

    fun get(path: String): String? = configs[path]

    private val configs = mapOf(
        "config/dev.json" to """
            {
              "environmentName": "DEV",
              "adminApiBaseUrl": "http://localhost:8081",
              "mobileApiBaseUrl": "http://localhost:9091",
              "webPort": 8080,
              "timeout": 30000,
              "debugMode": true,
              "mockMode": false
            }
        """.trimIndent(),

        "config/staging.json" to """
            {
              "environmentName": "STAGING",
              "adminApiBaseUrl": "https://edugo-api-admin.wittyhill-f6d656fb.eastus.azurecontainerapps.io",
              "mobileApiBaseUrl": "https://edugo-api-mobile.wittyhill-f6d656fb.eastus.azurecontainerapps.io",
              "webPort": 8080,
              "timeout": 60000,
              "debugMode": true,
              "mockMode": false
            }
        """.trimIndent(),

        "config/prod.json" to """
            {
              "environmentName": "PROD",
              "adminApiBaseUrl": "https://api.example.com",
              "mobileApiBaseUrl": "https://api-mobile.example.com",
              "webPort": 80,
              "timeout": 60000,
              "debugMode": false,
              "mockMode": false
            }
        """.trimIndent()
    )
}

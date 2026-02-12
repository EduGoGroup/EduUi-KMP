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
              "apiUrl": "http://localhost",
              "apiPort": 8080,
              "webPort": 8080,
              "timeout": 30000,
              "debugMode": true,
              "mockMode": false
            }
        """.trimIndent(),

        "config/staging.json" to """
            {
              "environmentName": "STAGING",
              "apiUrl": "https://api-staging.example.com",
              "apiPort": 443,
              "webPort": 8080,
              "timeout": 60000,
              "debugMode": true,
              "mockMode": false
            }
        """.trimIndent(),

        "config/prod.json" to """
            {
              "environmentName": "PROD",
              "apiUrl": "https://api.example.com",
              "apiPort": 443,
              "webPort": 80,
              "timeout": 60000,
              "debugMode": false,
              "mockMode": false
            }
        """.trimIndent()
    )
}

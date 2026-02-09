package com.edugo.kmp.config

/**
 * Implementación WasmJs para cargar recursos.
 *
 * En Wasm, los recursos deben ser embebidos en el bundle.
 * Por ahora retorna configuración hardcodeada.
 * Para producción, se puede usar fetch API para cargar desde servidor.
 */
internal actual fun loadResourceAsString(path: String): String? {
    return when (path) {
        "config/dev.json" -> """
            {
              "environmentName": "DEV",
              "apiUrl": "http://localhost",
              "apiPort": 8080,
              "webPort": 8080,
              "timeout": 30000,
              "debugMode": true
            }
        """.trimIndent()
        "config/staging.json" -> """
            {
              "environmentName": "STAGING",
              "apiUrl": "https://api-staging.example.com",
              "apiPort": 443,
              "webPort": 8080,
              "timeout": 60000,
              "debugMode": true
            }
        """.trimIndent()
        "config/prod.json" -> """
            {
              "environmentName": "PROD",
              "apiUrl": "https://api.example.com",
              "apiPort": 443,
              "webPort": 80,
              "timeout": 60000,
              "debugMode": false
            }
        """.trimIndent()
        else -> null
    }
}

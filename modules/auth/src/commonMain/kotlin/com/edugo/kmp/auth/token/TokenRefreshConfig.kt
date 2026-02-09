package com.edugo.kmp.auth.token

/**
 * Configuración para el comportamiento del [TokenRefreshManager].
 *
 * @property refreshThresholdSeconds Segundos antes de expiración para considerar refresh
 * @property maxRetryAttempts Máximo número de reintentos en caso de error de red
 * @property retryDelayMs Delay base para retry en millisegundos (se duplica en cada intento)
 * @property enableTokenRotation Si el backend soporta token rotation, actualizar refresh token
 */
public data class TokenRefreshConfig(
    val refreshThresholdSeconds: Int = 300,
    val maxRetryAttempts: Int = 3,
    val retryDelayMs: Long = 1000,
    val enableTokenRotation: Boolean = true
) {

    init {
        require(refreshThresholdSeconds >= 0) {
            "refreshThresholdSeconds debe ser >= 0, fue: $refreshThresholdSeconds"
        }
        require(maxRetryAttempts >= 0) {
            "maxRetryAttempts debe ser >= 0, fue: $maxRetryAttempts"
        }
        require(retryDelayMs >= 0) {
            "retryDelayMs debe ser >= 0, fue: $retryDelayMs"
        }
    }

    public fun calculateRetryDelay(attempt: Int): Long {
        if (attempt <= 0) return 0
        return retryDelayMs * (1 shl (attempt - 1))
    }

    public fun hasRetriesLeft(currentAttempt: Int): Boolean {
        return currentAttempt < maxRetryAttempts
    }

    public companion object {
        public val DEFAULT: TokenRefreshConfig = TokenRefreshConfig()

        public val DEVELOPMENT: TokenRefreshConfig = TokenRefreshConfig(
            refreshThresholdSeconds = 60,
            maxRetryAttempts = 10,
            retryDelayMs = 500,
            enableTokenRotation = true
        )

        public val CONSERVATIVE: TokenRefreshConfig = TokenRefreshConfig(
            refreshThresholdSeconds = 600,
            maxRetryAttempts = 5,
            retryDelayMs = 2000,
            enableTokenRotation = true
        )

        public val NO_RETRY: TokenRefreshConfig = TokenRefreshConfig(
            refreshThresholdSeconds = 300,
            maxRetryAttempts = 0,
            retryDelayMs = 0,
            enableTokenRotation = true
        )
    }
}

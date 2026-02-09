package com.edugo.kmp.auth.model

import com.edugo.kmp.foundation.result.Result
import com.edugo.kmp.foundation.result.failure
import com.edugo.kmp.foundation.result.success
import com.edugo.kmp.foundation.entity.ValidatableModel
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Modelo que representa un token de autenticación.
 *
 * @property token Token de autenticación (típicamente JWT)
 * @property expiresAt Timestamp de expiración del token (ISO-8601)
 * @property refreshToken Token opcional para renovar la autenticación (default: null)
 */
@Serializable
public data class AuthToken(
    @SerialName("token")
    val token: String,

    @SerialName("expires_at")
    val expiresAt: Instant = Clock.System.now(),

    @SerialName("refresh_token")
    val refreshToken: String? = null
) : ValidatableModel {

    override fun validate(): Result<Unit> {
        return when {
            token.isBlank() ->
                failure("Token cannot be blank")

            refreshToken != null && refreshToken.isBlank() ->
                failure("Refresh token cannot be blank if provided")

            else ->
                success(Unit)
        }
    }

    /**
     * Verifica si el token ha expirado.
     */
    public fun isExpired(): Boolean {
        return Clock.System.now() >= expiresAt
    }

    /**
     * Verifica si el token es válido (no vacío y no expirado).
     */
    public fun isValid(): Boolean {
        return validate() is Result.Success && !isExpired()
    }

    /**
     * Verifica si hay un refresh token disponible.
     */
    public fun hasRefreshToken(): Boolean {
        return !refreshToken.isNullOrBlank()
    }

    /**
     * Calcula el tiempo restante hasta la expiración.
     */
    public fun timeUntilExpiration(): kotlin.time.Duration {
        return expiresAt - Clock.System.now()
    }

    /**
     * Obtiene un resumen corto del token para logging.
     */
    public fun toLogString(): String {
        val tokenPreview = if (token.length > 10) {
            "${token.take(4)}...${token.takeLast(2)}"
        } else {
            "***"
        }
        return "AuthToken(token=$tokenPreview, expires=$expiresAt, hasRefresh=${hasRefreshToken()})"
    }

    companion object {
        /**
         * Crea un token de ejemplo para tests.
         */
        public fun createTestToken(
            durationSeconds: Long = 3600,
            includeRefresh: Boolean = true
        ): AuthToken {
            val now = Clock.System.now()
            return AuthToken(
                token = "test_token_${now.toEpochMilliseconds()}",
                expiresAt = now + kotlin.time.Duration.parse("${durationSeconds}s"),
                refreshToken = if (includeRefresh) "refresh_token_${now.toEpochMilliseconds()}" else null
            )
        }
    }
}

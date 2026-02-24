package com.edugo.kmp.auth.model

import kotlinx.datetime.Clock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.seconds

/**
 * Modelo que representa la respuesta exitosa del endpoint de login.
 *
 * @property accessToken Token de acceso JWT
 * @property expiresIn Tiempo de expiración en segundos desde ahora
 * @property refreshToken Token para renovar la sesión
 * @property tokenType Tipo de token (típicamente "Bearer")
 * @property user Información básica del usuario autenticado
 * @property activeContext Contexto RBAC activo (rol, permisos, escuela, etc.)
 */
@Serializable
public data class LoginResponse(
    @SerialName("access_token")
    val accessToken: String,

    @SerialName("expires_in")
    val expiresIn: Int,

    @SerialName("refresh_token")
    val refreshToken: String,

    @SerialName("token_type")
    val tokenType: String,

    @SerialName("user")
    val user: AuthUserInfo,

    @SerialName("active_context")
    val activeContext: UserContext,

    @SerialName("schools")
    val schools: List<SchoolInfo> = emptyList()
) {

    /**
     * Convierte este LoginResponse a un AuthToken.
     */
    public fun toAuthToken(): AuthToken {
        val now = Clock.System.now()
        val expiresAt = now + expiresIn.seconds

        return AuthToken(
            token = accessToken,
            expiresAt = expiresAt,
            refreshToken = refreshToken
        )
    }

    /**
     * Verifica si el token es de tipo Bearer.
     */
    public fun isBearerToken(): Boolean {
        return tokenType.equals("Bearer", ignoreCase = true)
    }

    /**
     * Obtiene el header de autorización formateado.
     */
    public fun getAuthorizationHeader(): String {
        return "$tokenType $accessToken"
    }

    /**
     * Calcula cuándo expirará el token.
     */
    public fun calculateExpirationTime(): kotlinx.datetime.Instant {
        return Clock.System.now() + expiresIn.seconds
    }

    /**
     * Obtiene una representación segura para logging.
     */
    public fun toLogString(): String {
        val tokenPreview = if (accessToken.length > 10) {
            "${accessToken.take(4)}...${accessToken.takeLast(2)}"
        } else {
            "***"
        }
        return "LoginResponse(tokenType=$tokenType, expiresIn=$expiresIn, " +
                "token=$tokenPreview, userId=${user.id}, userRole=${activeContext.roleName})"
    }

    companion object {
        /**
         * Crea una respuesta de ejemplo para tests.
         */
        public fun createTestResponse(
            accessToken: String = "test_access_token_${Clock.System.now().toEpochMilliseconds()}",
            expiresIn: Int = 3600,
            refreshToken: String = "test_refresh_token_${Clock.System.now().toEpochMilliseconds()}",
            user: AuthUserInfo = AuthUserInfo.createTestUser(),
            activeContext: UserContext = UserContext.createTestContext(),
            schools: List<SchoolInfo> = emptyList()
        ): LoginResponse {
            return LoginResponse(
                accessToken = accessToken,
                expiresIn = expiresIn,
                refreshToken = refreshToken,
                tokenType = "Bearer",
                user = user,
                activeContext = activeContext,
                schools = schools
            )
        }
    }
}

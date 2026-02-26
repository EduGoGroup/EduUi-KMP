package com.edugo.kmp.auth.repository

import com.edugo.kmp.auth.model.AuthToken
import com.edugo.kmp.auth.model.UserContext
import kotlinx.datetime.Clock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.seconds

@Serializable
public data class RefreshResponse(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("expires_in")
    val expiresIn: Int,
    @SerialName("token_type")
    val tokenType: String,
    @SerialName("refresh_token")
    val refreshToken: String? = null,
    @SerialName("active_context")
    val activeContext: UserContext? = null
) {
    public fun toAuthToken(existingRefreshToken: String): AuthToken {
        val now = Clock.System.now()
        val expiresAt = now + expiresIn.seconds
        return AuthToken(token = accessToken, expiresAt = expiresAt, refreshToken = refreshToken ?: existingRefreshToken)
    }

    public fun isBearerToken(): Boolean = tokenType.equals("Bearer", ignoreCase = true)
    public fun getAuthorizationHeader(): String = "$tokenType $accessToken"
    public fun calculateExpirationTime(): kotlinx.datetime.Instant = Clock.System.now() + expiresIn.seconds

    public fun toLogString(): String {
        val tokenPreview = if (accessToken.length > 10) "${accessToken.take(4)}...${accessToken.takeLast(2)}" else "***"
        return "RefreshResponse(tokenType=$tokenType, expiresIn=$expiresIn, token=$tokenPreview)"
    }

    companion object {
        public fun createTestResponse(
            accessToken: String = "test_access_token_${Clock.System.now().toEpochMilliseconds()}",
            expiresIn: Int = 3600,
            tokenType: String = "Bearer"
        ): RefreshResponse = RefreshResponse(accessToken = accessToken, expiresIn = expiresIn, tokenType = tokenType)
    }
}

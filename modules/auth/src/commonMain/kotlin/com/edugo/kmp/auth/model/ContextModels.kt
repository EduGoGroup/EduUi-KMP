package com.edugo.kmp.auth.model

import kotlinx.datetime.Clock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.seconds

/**
 * Response from GET /v1/auth/contexts
 * Contains the current context and all available contexts for the user.
 */
@Serializable
public data class AvailableContextsResponse(
    @SerialName("current")
    val current: UserContext? = null,

    @SerialName("available")
    val available: List<UserContext> = emptyList()
) {
    /** Returns true if the user has more than one available context */
    public fun hasMultipleContexts(): Boolean = available.size > 1
}

/**
 * Response from POST /v1/auth/switch-context
 * Contains new tokens and the updated context.
 */
@Serializable
public data class SwitchContextResponse(
    @SerialName("access_token")
    val accessToken: String,

    @SerialName("refresh_token")
    val refreshToken: String,

    @SerialName("expires_in")
    val expiresIn: Int,

    @SerialName("token_type")
    val tokenType: String,

    @SerialName("context")
    val context: SwitchContextInfo? = null
) {
    /** Converts to AuthToken for internal use */
    public fun toAuthToken(): AuthToken {
        val now = Clock.System.now()
        val expiresAt = now + expiresIn.seconds
        return AuthToken(token = accessToken, expiresAt = expiresAt, refreshToken = refreshToken)
    }
}

/**
 * Context info returned from switch-context endpoint.
 */
@Serializable
public data class SwitchContextInfo(
    @SerialName("school_id")
    val schoolId: String = "",

    @SerialName("school_name")
    val schoolName: String = "",

    @SerialName("role")
    val role: String = "",

    @SerialName("user_id")
    val userId: String = "",

    @SerialName("email")
    val email: String = ""
)

package com.edugo.kmp.auth.repository

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class TokenVerificationResponse(
    @SerialName("valid") val valid: Boolean,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("email") val email: String? = null,
    @SerialName("role") val role: String? = null,
    @SerialName("school_id") val schoolId: String? = null,
    @SerialName("expires_at") val expiresAt: String? = null,
    @SerialName("error") val error: String? = null
)

@Serializable
public data class TokenVerificationRequest(
    @SerialName("token") val token: String
)

package com.edugo.kmp.auth.token

import com.edugo.kmp.auth.model.AuthError
import com.edugo.kmp.foundation.error.ErrorCode

/**
 * Sealed class que representa las razones por las cuales un refresh de token puede fallar.
 */
public sealed class RefreshFailureReason {

    public object TokenExpired : RefreshFailureReason()

    public object TokenRevoked : RefreshFailureReason()

    public object NoRefreshToken : RefreshFailureReason()

    public data class NetworkError(val cause: String) : RefreshFailureReason()

    public data class ServerError(val code: Int, val message: String) : RefreshFailureReason()

    public fun toAuthError(): AuthError {
        return when (this) {
            is TokenExpired -> AuthError.fromErrorResponse(
                code = ErrorCode.AUTH_TOKEN_EXPIRED.name,
                message = ErrorCode.AUTH_TOKEN_EXPIRED.description
            )

            is TokenRevoked -> AuthError.fromErrorResponse(
                code = ErrorCode.AUTH_TOKEN_REVOKED.name,
                message = ErrorCode.AUTH_TOKEN_REVOKED.description
            )

            is NoRefreshToken -> AuthError.fromErrorResponse(
                code = ErrorCode.AUTH_REFRESH_TOKEN_INVALID.name,
                message = "No refresh token available"
            )

            is NetworkError -> AuthError.NetworkError(cause)

            is ServerError -> AuthError.UnknownError("Server error: $code - $message")
        }
    }

    public fun isRetryable(): Boolean {
        return when (this) {
            is NetworkError -> true
            is ServerError -> code >= 500
            is TokenExpired,
            is TokenRevoked,
            is NoRefreshToken -> false
        }
    }

    public fun toLogString(): String {
        return when (this) {
            is TokenExpired -> "RefreshFailureReason.TokenExpired"
            is TokenRevoked -> "RefreshFailureReason.TokenRevoked"
            is NoRefreshToken -> "RefreshFailureReason.NoRefreshToken"
            is NetworkError -> "RefreshFailureReason.NetworkError(cause=$cause)"
            is ServerError -> "RefreshFailureReason.ServerError(code=$code, message=$message)"
        }
    }
}

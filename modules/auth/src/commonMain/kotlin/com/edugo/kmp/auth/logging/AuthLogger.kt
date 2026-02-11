package com.edugo.kmp.auth.logging

import com.edugo.kmp.logger.Logger

public class AuthLogger(
    private val logger: Logger
) {
    private companion object {
        private const val TAG = "Auth"
    }

    public fun logLoginAttempt(email: String) {
        logger.i(TAG, "LOGIN_ATTEMPT | user=$email")
    }

    public fun logLoginSuccess(email: String, userId: String) {
        logger.i(TAG, "LOGIN_SUCCESS | user=$email | userId=$userId")
    }

    public fun logLoginFailure(email: String, reason: String) {
        logger.w(TAG, "LOGIN_FAILURE | user=$email | reason=$reason")
    }

    public fun logLogout(userId: String?) {
        logger.i(TAG, "LOGOUT | userId=${userId ?: "unknown"}")
    }

    public fun logTokenRefresh(success: Boolean) {
        if (success) {
            logger.d(TAG, "TOKEN_REFRESH | status=success")
        } else {
            logger.w(TAG, "TOKEN_REFRESH | status=failure")
        }
    }

    public fun logSessionExpired(reason: String) {
        logger.w(TAG, "SESSION_EXPIRED | reason=$reason")
    }

    public fun logSessionRestored(userId: String) {
        logger.i(TAG, "SESSION_RESTORED | userId=$userId")
    }

    public fun logAutoRefreshStarted() {
        logger.d(TAG, "AUTO_REFRESH | status=started")
    }

    public fun logAutoRefreshStopped() {
        logger.d(TAG, "AUTO_REFRESH | status=stopped")
    }
}

package com.edugo.kmp.auth.config

import com.edugo.kmp.auth.circuit.CircuitBreakerConfig
import com.edugo.kmp.auth.retry.RetryPolicy
import com.edugo.kmp.auth.token.TokenRefreshConfig
import com.edugo.kmp.config.Environment
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

public data class AuthConfig(
    val refreshConfig: TokenRefreshConfig,
    val circuitBreakerConfig: CircuitBreakerConfig,
    val retryPolicy: RetryPolicy,
    val rateLimitMaxRequests: Int,
    val rateLimitWindow: Duration
) {
    public companion object {
        public fun forEnvironment(env: Environment): AuthConfig {
            return when (env) {
                Environment.DEV -> AuthConfig(
                    refreshConfig = TokenRefreshConfig.DEVELOPMENT,
                    circuitBreakerConfig = CircuitBreakerConfig.development(),
                    retryPolicy = RetryPolicy.AGGRESSIVE,
                    rateLimitMaxRequests = 20,
                    rateLimitWindow = 1.minutes
                )
                Environment.STAGING -> AuthConfig(
                    refreshConfig = TokenRefreshConfig.DEFAULT,
                    circuitBreakerConfig = CircuitBreakerConfig.default(),
                    retryPolicy = RetryPolicy.DEFAULT,
                    rateLimitMaxRequests = 10,
                    rateLimitWindow = 1.minutes
                )
                Environment.PROD -> AuthConfig(
                    refreshConfig = TokenRefreshConfig.CONSERVATIVE,
                    circuitBreakerConfig = CircuitBreakerConfig.conservative(),
                    retryPolicy = RetryPolicy.CONSERVATIVE,
                    rateLimitMaxRequests = 5,
                    rateLimitWindow = 1.minutes
                )
            }
        }
    }
}

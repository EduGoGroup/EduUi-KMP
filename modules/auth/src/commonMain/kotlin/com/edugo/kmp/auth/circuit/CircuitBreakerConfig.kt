package com.edugo.kmp.auth.circuit

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

public data class CircuitBreakerConfig(
    val failureThreshold: Int = 5,
    val successThreshold: Int = 2,
    val timeout: Duration = 30.seconds
) {
    init {
        require(failureThreshold > 0) { "failureThreshold debe ser > 0" }
        require(successThreshold > 0) { "successThreshold debe ser > 0" }
        require(timeout.isPositive()) { "timeout debe ser positivo" }
    }

    public companion object {
        public fun default(): CircuitBreakerConfig = CircuitBreakerConfig()

        public fun development(): CircuitBreakerConfig = CircuitBreakerConfig(
            failureThreshold = 10,
            successThreshold = 1,
            timeout = 15.seconds
        )

        public fun conservative(): CircuitBreakerConfig = CircuitBreakerConfig(
            failureThreshold = 3,
            successThreshold = 3,
            timeout = 60.seconds
        )
    }
}

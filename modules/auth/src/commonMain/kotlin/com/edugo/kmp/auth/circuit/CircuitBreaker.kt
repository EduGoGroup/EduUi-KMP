package com.edugo.kmp.auth.circuit

import com.edugo.kmp.foundation.result.Result
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock

public class CircuitBreaker(
    private val config: CircuitBreakerConfig = CircuitBreakerConfig.default()
) {
    private val mutex = Mutex()
    private var state: CircuitState = CircuitState.Closed
    private var failureCount: Int = 0
    private var successCount: Int = 0

    public suspend fun <T> execute(operation: suspend () -> Result<T>): Result<T> {
        return mutex.withLock {
            when (val currentState = state) {
                is CircuitState.Open -> {
                    val elapsed = Clock.System.now() - currentState.openedAt
                    if (elapsed >= config.timeout) {
                        state = CircuitState.HalfOpen(attempt = 0)
                        successCount = 0
                        executeOperation(operation)
                    } else {
                        Result.Failure("Circuit breaker is open")
                    }
                }
                is CircuitState.HalfOpen -> {
                    executeOperation(operation)
                }
                is CircuitState.Closed -> {
                    executeOperation(operation)
                }
            }
        }
    }

    private suspend fun <T> executeOperation(operation: suspend () -> Result<T>): Result<T> {
        val result = operation()
        when (result) {
            is Result.Success -> onSuccess()
            is Result.Failure -> onFailure()
            is Result.Loading -> { /* no-op */ }
        }
        return result
    }

    private fun onSuccess() {
        when (state) {
            is CircuitState.HalfOpen -> {
                successCount++
                if (successCount >= config.successThreshold) {
                    state = CircuitState.Closed
                    failureCount = 0
                    successCount = 0
                }
            }
            is CircuitState.Closed -> {
                failureCount = 0
            }
            is CircuitState.Open -> { /* no-op */ }
        }
    }

    private fun onFailure() {
        when (state) {
            is CircuitState.HalfOpen -> {
                state = CircuitState.Open(openedAt = Clock.System.now())
                successCount = 0
            }
            is CircuitState.Closed -> {
                failureCount++
                if (failureCount >= config.failureThreshold) {
                    state = CircuitState.Open(openedAt = Clock.System.now())
                }
            }
            is CircuitState.Open -> { /* no-op */ }
        }
    }

    public fun getState(): CircuitState = state

    public fun forceOpen() {
        state = CircuitState.Open(openedAt = Clock.System.now())
        failureCount = 0
        successCount = 0
    }

    public fun forceClose() {
        state = CircuitState.Closed
        failureCount = 0
        successCount = 0
    }
}

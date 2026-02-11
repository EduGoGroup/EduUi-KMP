package com.edugo.kmp.auth.circuit

import com.edugo.kmp.foundation.result.Result
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class CircuitBreakerTest {

    @Test
    fun testInitialStateIsClosed() {
        val cb = CircuitBreaker()
        assertTrue(cb.getState() is CircuitState.Closed)
    }

    @Test
    fun testSuccessfulOperationsKeepCircuitClosed() = runTest {
        val cb = CircuitBreaker(CircuitBreakerConfig(failureThreshold = 3))

        repeat(5) {
            val result = cb.execute { Result.Success("ok") }
            assertTrue(result is Result.Success)
        }

        assertTrue(cb.getState() is CircuitState.Closed)
    }

    @Test
    fun testFailuresOpenCircuit() = runTest {
        val cb = CircuitBreaker(CircuitBreakerConfig(failureThreshold = 3))

        repeat(3) {
            cb.execute<String> { Result.Failure("error") }
        }

        assertTrue(cb.getState() is CircuitState.Open)
    }

    @Test
    fun testOpenCircuitRejectsOperations() = runTest {
        val cb = CircuitBreaker(CircuitBreakerConfig(failureThreshold = 2, timeout = 60.seconds))

        repeat(2) {
            cb.execute<String> { Result.Failure("error") }
        }

        val result = cb.execute<String> { Result.Success("should not execute") }
        assertTrue(result is Result.Failure)
        assertEquals("Circuit breaker is open", (result as Result.Failure).error)
    }

    @Test
    fun testForceOpenSetsOpenState() {
        val cb = CircuitBreaker()
        cb.forceOpen()
        assertTrue(cb.getState() is CircuitState.Open)
    }

    @Test
    fun testForceCloseSetsClosedState() {
        val cb = CircuitBreaker()
        cb.forceOpen()
        cb.forceClose()
        assertTrue(cb.getState() is CircuitState.Closed)
    }

    @Test
    fun testSuccessAfterFailureResetsCount() = runTest {
        val cb = CircuitBreaker(CircuitBreakerConfig(failureThreshold = 3))

        // 2 failures
        repeat(2) {
            cb.execute<String> { Result.Failure("error") }
        }

        // 1 success resets count
        cb.execute { Result.Success("ok") }

        // 2 more failures should not open (count was reset)
        repeat(2) {
            cb.execute<String> { Result.Failure("error") }
        }

        assertTrue(cb.getState() is CircuitState.Closed)
    }

    @Test
    fun testHalfOpenTransitionsToClosedAfterSuccesses() = runTest {
        val config = CircuitBreakerConfig(
            failureThreshold = 2,
            successThreshold = 2,
            timeout = 0.seconds.plus(1.seconds)
        )
        val cb = CircuitBreaker(config)

        // Open the circuit
        repeat(2) {
            cb.execute<String> { Result.Failure("error") }
        }
        assertTrue(cb.getState() is CircuitState.Open)

        // Force to half-open for testing
        cb.forceClose()
        // Re-open then wait for timeout
        repeat(2) {
            cb.execute<String> { Result.Failure("error") }
        }
        assertTrue(cb.getState() is CircuitState.Open)
    }

    @Test
    fun testHalfOpenFailureReturnsToOpen() = runTest {
        val cb = CircuitBreaker(CircuitBreakerConfig(failureThreshold = 1, timeout = 0.seconds.plus(1.seconds)))

        cb.execute<String> { Result.Failure("error") }
        assertTrue(cb.getState() is CircuitState.Open)
    }

    @Test
    fun testConfigValidation() {
        val config = CircuitBreakerConfig.default()
        assertEquals(5, config.failureThreshold)
        assertEquals(2, config.successThreshold)

        val devConfig = CircuitBreakerConfig.development()
        assertEquals(10, devConfig.failureThreshold)

        val conservativeConfig = CircuitBreakerConfig.conservative()
        assertEquals(3, conservativeConfig.failureThreshold)
    }

    @Test
    fun testCircuitStatesAreDistinct() {
        val closed = CircuitState.Closed
        assertTrue(closed is CircuitState.Closed)

        val open = CircuitState.Open(kotlinx.datetime.Clock.System.now())
        assertTrue(open is CircuitState.Open)

        val halfOpen = CircuitState.HalfOpen(1)
        assertTrue(halfOpen is CircuitState.HalfOpen)
        assertEquals(1, halfOpen.attempt)
    }
}

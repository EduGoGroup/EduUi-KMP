package com.edugo.kmp.auth.retry

import com.edugo.kmp.foundation.result.Result
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class RetryPolicyTest {

    @Test
    fun testSucceedsOnFirstAttempt() = runTest {
        val policy = RetryPolicy.DEFAULT
        var attemptCount = 0

        val result = withRetry(policy) { attempt ->
            attemptCount = attempt
            Result.Success("ok")
        }

        assertTrue(result is Result.Success)
        assertEquals("ok", (result as Result.Success).data)
        assertEquals(1, attemptCount)
    }

    @Test
    fun testRetriesOnRetryableError() = runTest {
        val policy = RetryPolicy(
            maxAttempts = 3,
            initialDelay = 10.milliseconds,
            maxDelay = 100.milliseconds
        )
        var attemptCount = 0

        val result = withRetry(policy) { attempt ->
            attemptCount = attempt
            if (attempt < 3) {
                Result.Failure("network timeout")
            } else {
                Result.Success("recovered")
            }
        }

        assertTrue(result is Result.Success)
        assertEquals("recovered", (result as Result.Success).data)
        assertEquals(3, attemptCount)
    }

    @Test
    fun testDoesNotRetryOnNonRetryableError() = runTest {
        val policy = RetryPolicy(
            maxAttempts = 3,
            initialDelay = 10.milliseconds,
            maxDelay = 100.milliseconds
        )
        var attemptCount = 0

        val result = withRetry(policy) { attempt ->
            attemptCount = attempt
            Result.Failure("invalid credentials")
        }

        assertTrue(result is Result.Failure)
        assertEquals(1, attemptCount)
    }

    @Test
    fun testRespectsMaxAttempts() = runTest {
        val policy = RetryPolicy(
            maxAttempts = 3,
            initialDelay = 10.milliseconds,
            maxDelay = 100.milliseconds
        )
        var attemptCount = 0

        val result = withRetry(policy) { attempt ->
            attemptCount = attempt
            Result.Failure("connection timeout")
        }

        assertTrue(result is Result.Failure)
        assertEquals(3, attemptCount)
        assertTrue((result as Result.Failure).error.contains("timeout"))
    }

    @Test
    fun testNoRetryPolicyOnlyRunsOnce() = runTest {
        val policy = RetryPolicy.NO_RETRY
        var attemptCount = 0

        val result = withRetry(policy) { attempt ->
            attemptCount = attempt
            Result.Failure("network error")
        }

        assertTrue(result is Result.Failure)
        assertEquals(1, attemptCount)
    }

    @Test
    fun testIsRetryableMatchesConfiguredErrors() {
        val policy = RetryPolicy.DEFAULT

        assertTrue(policy.isRetryable("network error"))
        assertTrue(policy.isRetryable("Connection timeout"))
        assertTrue(policy.isRetryable("Socket reset"))
        assertTrue(policy.isRetryable("Host unreachable"))
        assertTrue(policy.isRetryable("Error 503: Service unavailable"))
        assertTrue(policy.isRetryable("Error 502: Bad gateway"))

        assertFalse(policy.isRetryable("invalid credentials"))
        assertFalse(policy.isRetryable("user not found"))
        assertFalse(policy.isRetryable("forbidden"))
    }

    @Test
    fun testDelayForAttemptAppliesExponentialBackoff() {
        val policy = RetryPolicy(
            initialDelay = 100.milliseconds,
            maxDelay = 10.seconds,
            backoffMultiplier = 2.0
        )

        assertEquals(100.milliseconds, policy.delayForAttempt(1))
        assertEquals(200.milliseconds, policy.delayForAttempt(2))
        assertEquals(400.milliseconds, policy.delayForAttempt(3))
        assertEquals(800.milliseconds, policy.delayForAttempt(4))
    }

    @Test
    fun testDelayRespectsMaxDelay() {
        val policy = RetryPolicy(
            initialDelay = 1.seconds,
            maxDelay = 3.seconds,
            backoffMultiplier = 4.0
        )

        assertEquals(1.seconds, policy.delayForAttempt(1))
        assertEquals(3.seconds, policy.delayForAttempt(2))
        assertEquals(3.seconds, policy.delayForAttempt(3))
    }

    @Test
    fun testPresetsHaveExpectedValues() {
        val default = RetryPolicy.DEFAULT
        assertEquals(3, default.maxAttempts)
        assertEquals(500.milliseconds, default.initialDelay)

        val aggressive = RetryPolicy.AGGRESSIVE
        assertEquals(5, aggressive.maxAttempts)
        assertEquals(200.milliseconds, aggressive.initialDelay)

        val conservative = RetryPolicy.CONSERVATIVE
        assertEquals(2, conservative.maxAttempts)
        assertEquals(1.seconds, conservative.initialDelay)

        val noRetry = RetryPolicy.NO_RETRY
        assertEquals(1, noRetry.maxAttempts)
    }

    @Test
    fun testRetryWithServerError502() = runTest {
        val policy = RetryPolicy(
            maxAttempts = 2,
            initialDelay = 10.milliseconds,
            maxDelay = 50.milliseconds
        )
        var attemptCount = 0

        val result = withRetry(policy) { attempt ->
            attemptCount = attempt
            if (attempt < 2) {
                Result.Failure("502 Bad Gateway")
            } else {
                Result.Success("ok")
            }
        }

        assertTrue(result is Result.Success)
        assertEquals(2, attemptCount)
    }
}

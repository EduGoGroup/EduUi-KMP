package com.edugo.kmp.auth.throttle

import com.edugo.kmp.foundation.result.Result
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes

class RateLimiterTest {

    @Test
    fun testAllowsRequestsUnderLimit() = runTest {
        val rateLimiter = RateLimiter(maxRequests = 3, timeWindow = 1.minutes)

        repeat(3) {
            val result = rateLimiter.execute { Result.Success("ok") }
            assertTrue(result is Result.Success)
        }
    }

    @Test
    fun testBlocksRequestsOverLimit() = runTest {
        val rateLimiter = RateLimiter(maxRequests = 2, timeWindow = 1.minutes)

        rateLimiter.execute { Result.Success("ok") }
        rateLimiter.execute { Result.Success("ok") }

        val result = rateLimiter.execute { Result.Success("should not execute") }
        assertTrue(result is Result.Failure)
        assertEquals("Rate limit exceeded", (result as Result.Failure).error)
    }

    @Test
    fun testRemainingRequestsDecrements() = runTest {
        val rateLimiter = RateLimiter(maxRequests = 5, timeWindow = 1.minutes)

        assertEquals(5, rateLimiter.remainingRequests())

        rateLimiter.execute { Result.Success("ok") }
        assertEquals(4, rateLimiter.remainingRequests())

        rateLimiter.execute { Result.Success("ok") }
        assertEquals(3, rateLimiter.remainingRequests())
    }

    @Test
    fun testResetClearsAllTimestamps() = runTest {
        val rateLimiter = RateLimiter(maxRequests = 2, timeWindow = 1.minutes)

        rateLimiter.execute { Result.Success("ok") }
        rateLimiter.execute { Result.Success("ok") }
        assertEquals(0, rateLimiter.remainingRequests())

        rateLimiter.reset()
        assertEquals(2, rateLimiter.remainingRequests())
    }

    @Test
    fun testFailedOperationsStillCountAgainstLimit() = runTest {
        val rateLimiter = RateLimiter(maxRequests = 2, timeWindow = 1.minutes)

        rateLimiter.execute<String> { Result.Failure("error") }
        rateLimiter.execute<String> { Result.Failure("error") }

        val result = rateLimiter.execute<String> { Result.Success("should not execute") }
        assertTrue(result is Result.Failure)
        assertEquals("Rate limit exceeded", (result as Result.Failure).error)
    }

    @Test
    fun testRemainingRequestsNeverNegative() = runTest {
        val rateLimiter = RateLimiter(maxRequests = 1, timeWindow = 1.minutes)

        rateLimiter.execute { Result.Success("ok") }
        assertEquals(0, rateLimiter.remainingRequests())

        // Even after more attempts, remaining should stay at 0
        rateLimiter.execute<String> { Result.Success("blocked") }
        assertEquals(0, rateLimiter.remainingRequests())
    }

    @Test
    fun testInitialRemainingEqualsMax() {
        val rateLimiter = RateLimiter(maxRequests = 10, timeWindow = 1.minutes)
        assertEquals(10, rateLimiter.remainingRequests())
    }

    @Test
    fun testSingleRequestAllowed() = runTest {
        val rateLimiter = RateLimiter(maxRequests = 1, timeWindow = 1.minutes)

        val result = rateLimiter.execute { Result.Success(42) }
        assertTrue(result is Result.Success)
        assertEquals(42, (result as Result.Success).data)
    }
}

package com.edugo.kmp.auth.throttle

import com.edugo.kmp.foundation.result.Result
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

public class RateLimiter(
    private val maxRequests: Int = 5,
    private val timeWindow: Duration = 1.minutes
) {
    private val mutex = Mutex()
    private val timestamps = mutableListOf<Instant>()

    init {
        require(maxRequests > 0) { "maxRequests debe ser > 0" }
        require(timeWindow.isPositive()) { "timeWindow debe ser positivo" }
    }

    public suspend fun <T> execute(operation: suspend () -> Result<T>): Result<T> {
        return mutex.withLock {
            cleanExpired()
            if (timestamps.size >= maxRequests) {
                Result.Failure("Rate limit exceeded")
            } else {
                timestamps.add(Clock.System.now())
                operation()
            }
        }
    }

    public fun remainingRequests(): Int {
        cleanExpired()
        return (maxRequests - timestamps.size).coerceAtLeast(0)
    }

    public fun reset() {
        timestamps.clear()
    }

    private fun cleanExpired() {
        val cutoff = Clock.System.now() - timeWindow
        timestamps.removeAll { it < cutoff }
    }
}

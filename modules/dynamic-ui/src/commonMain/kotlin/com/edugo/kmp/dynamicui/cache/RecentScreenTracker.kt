package com.edugo.kmp.dynamicui.cache

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class RecentScreenTracker(
    private val maxEntries: Int = 10,
    private val clock: Clock = Clock.System,
) {
    private val accessMap = LinkedHashMap<String, Instant>()

    fun recordAccess(screenKey: String) {
        accessMap.remove(screenKey)
        accessMap[screenKey] = clock.now()
        if (accessMap.size > maxEntries) {
            val oldest = accessMap.keys.first()
            accessMap.remove(oldest)
        }
    }

    fun getRecentKeys(within: Duration = 15.minutes): List<String> {
        val cutoff = clock.now() - within
        return accessMap.filter { it.value >= cutoff }.keys.toList()
    }

    fun clear() {
        accessMap.clear()
    }
}

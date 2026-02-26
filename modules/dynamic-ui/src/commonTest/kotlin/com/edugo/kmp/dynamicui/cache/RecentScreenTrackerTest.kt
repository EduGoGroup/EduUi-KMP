package com.edugo.kmp.dynamicui.cache

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class RecentScreenTrackerTest {

    private class FakeClock(var now: Instant = Instant.fromEpochMilliseconds(1000000)) : Clock {
        override fun now(): Instant = now
        fun advance(duration: Duration) { now = now + duration }
    }

    @Test
    fun recordAccess_adds_entry() {
        val tracker = RecentScreenTracker()
        tracker.recordAccess("schools-list")
        assertEquals(listOf("schools-list"), tracker.getRecentKeys())
    }

    @Test
    fun recordAccess_updates_timestamp_on_duplicate() {
        val clock = FakeClock()
        val tracker = RecentScreenTracker(clock = clock)

        tracker.recordAccess("screen-a")
        clock.advance(1.minutes)
        tracker.recordAccess("screen-b")
        clock.advance(1.minutes)
        tracker.recordAccess("screen-a")

        val recent = tracker.getRecentKeys()
        assertEquals(listOf("screen-b", "screen-a"), recent)
    }

    @Test
    fun getRecentKeys_filters_by_time() {
        val clock = FakeClock()
        val tracker = RecentScreenTracker(clock = clock)

        tracker.recordAccess("old-screen")
        clock.advance(20.minutes)
        tracker.recordAccess("new-screen")

        val recent = tracker.getRecentKeys(within = 15.minutes)
        assertEquals(listOf("new-screen"), recent)
    }

    @Test
    fun getRecentKeys_returns_empty_when_all_expired() {
        val clock = FakeClock()
        val tracker = RecentScreenTracker(clock = clock)

        tracker.recordAccess("screen-a")
        clock.advance(20.minutes)

        assertTrue(tracker.getRecentKeys(within = 15.minutes).isEmpty())
    }

    @Test
    fun maxEntries_evicts_oldest() {
        val tracker = RecentScreenTracker(maxEntries = 3)
        tracker.recordAccess("a")
        tracker.recordAccess("b")
        tracker.recordAccess("c")
        tracker.recordAccess("d")

        val recent = tracker.getRecentKeys()
        assertEquals(3, recent.size)
        assertEquals(listOf("b", "c", "d"), recent)
    }

    @Test
    fun clear_removes_all_entries() {
        val tracker = RecentScreenTracker()
        tracker.recordAccess("a")
        tracker.recordAccess("b")
        tracker.clear()
        assertTrue(tracker.getRecentKeys().isEmpty())
    }
}

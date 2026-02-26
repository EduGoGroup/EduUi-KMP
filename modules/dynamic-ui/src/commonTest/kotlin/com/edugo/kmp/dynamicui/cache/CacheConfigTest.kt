package com.edugo.kmp.dynamicui.cache

import com.edugo.kmp.dynamicui.model.ScreenPattern
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class CacheConfigTest {

    @Test
    fun screenTtlFor_returns_pattern_default_for_dashboard() {
        val config = CacheConfig()
        assertEquals(60.seconds, config.screenTtlFor(ScreenPattern.DASHBOARD, null))
    }

    @Test
    fun screenTtlFor_returns_pattern_default_for_list() {
        val config = CacheConfig()
        assertEquals(5.minutes, config.screenTtlFor(ScreenPattern.LIST, null))
    }

    @Test
    fun screenTtlFor_returns_global_default_for_unknown_pattern() {
        val config = CacheConfig()
        assertEquals(CacheConfig.DEFAULT_SCREEN_TTL, config.screenTtlFor(ScreenPattern.SEARCH, null))
    }

    @Test
    fun screenTtlFor_returns_global_default_when_null_pattern() {
        val config = CacheConfig()
        assertEquals(CacheConfig.DEFAULT_SCREEN_TTL, config.screenTtlFor(null, null))
    }

    @Test
    fun screenTtlFor_overrides_take_priority_over_pattern() {
        val config = CacheConfig(
            screenTtlOverrides = mapOf("my-screen" to 15.minutes)
        )
        assertEquals(15.minutes, config.screenTtlFor(ScreenPattern.DASHBOARD, "my-screen"))
    }

    @Test
    fun dataTtlFor_returns_pattern_default() {
        val config = CacheConfig()
        assertEquals(60.minutes, config.dataTtlFor(ScreenPattern.FORM, null))
    }

    @Test
    fun dataTtlFor_overrides_take_priority() {
        val config = CacheConfig(
            dataTtlOverrides = mapOf("schools-list" to 2.minutes)
        )
        assertEquals(2.minutes, config.dataTtlFor(ScreenPattern.LIST, "schools-list"))
    }

    @Test
    fun dataTtlFor_returns_global_default_for_null_inputs() {
        val config = CacheConfig()
        assertEquals(CacheConfig.DEFAULT_DATA_TTL, config.dataTtlFor(null, null))
    }

    @Test
    fun custom_max_entries_are_stored() {
        val config = CacheConfig(maxScreenMemoryEntries = 50, maxDataMemoryEntries = 100)
        assertEquals(50, config.maxScreenMemoryEntries)
        assertEquals(100, config.maxDataMemoryEntries)
    }
}

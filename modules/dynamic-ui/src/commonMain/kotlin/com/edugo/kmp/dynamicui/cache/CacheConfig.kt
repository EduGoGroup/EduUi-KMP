package com.edugo.kmp.dynamicui.cache

import com.edugo.kmp.dynamicui.model.ScreenPattern
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class CacheConfig(
    val screenTtlByPattern: Map<ScreenPattern, Duration> = DEFAULT_SCREEN_TTLS,
    val dataTtlByPattern: Map<ScreenPattern, Duration> = DEFAULT_DATA_TTLS,
    val screenTtlOverrides: Map<String, Duration> = emptyMap(),
    val dataTtlOverrides: Map<String, Duration> = emptyMap(),
    val maxScreenMemoryEntries: Int = 20,
    val maxDataMemoryEntries: Int = 30,
) {
    fun screenTtlFor(pattern: ScreenPattern?, screenKey: String?): Duration {
        screenKey?.let { screenTtlOverrides[it] }?.let { return it }
        pattern?.let { screenTtlByPattern[it] }?.let { return it }
        return DEFAULT_SCREEN_TTL
    }

    fun dataTtlFor(pattern: ScreenPattern?, screenKey: String?): Duration {
        screenKey?.let { dataTtlOverrides[it] }?.let { return it }
        pattern?.let { dataTtlByPattern[it] }?.let { return it }
        return DEFAULT_DATA_TTL
    }

    companion object {
        val DEFAULT_SCREEN_TTL: Duration = 5.minutes
        val DEFAULT_DATA_TTL: Duration = 5.minutes

        val DEFAULT_SCREEN_TTLS = mapOf(
            ScreenPattern.DASHBOARD to 60.seconds,
            ScreenPattern.LIST to 5.minutes,
            ScreenPattern.FORM to 60.minutes,
            ScreenPattern.DETAIL to 10.minutes,
            ScreenPattern.SETTINGS to 30.minutes,
        )

        val DEFAULT_DATA_TTLS = mapOf(
            ScreenPattern.DASHBOARD to 60.seconds,
            ScreenPattern.LIST to 5.minutes,
            ScreenPattern.FORM to 60.minutes,
            ScreenPattern.DETAIL to 10.minutes,
            ScreenPattern.SETTINGS to 30.minutes,
        )
    }
}

package com.edugo.kmp.dynamicui.loader

import com.edugo.kmp.dynamicui.model.ControlType
import com.edugo.kmp.dynamicui.model.NavigationDefinition
import com.edugo.kmp.dynamicui.model.ScreenDefinition
import com.edugo.kmp.dynamicui.model.ScreenPattern
import com.edugo.kmp.dynamicui.model.ScreenTemplate
import com.edugo.kmp.dynamicui.model.Slot
import com.edugo.kmp.dynamicui.model.Zone
import com.edugo.kmp.dynamicui.model.ZoneType
import com.edugo.kmp.foundation.result.Result
import com.edugo.kmp.storage.EduGoStorage
import com.edugo.kmp.storage.SafeEduGoStorage
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class CachedScreenLoaderTest {

    private fun createTestScreen(key: String = "test-screen"): ScreenDefinition {
        return ScreenDefinition(
            screenId = "scr-001",
            screenKey = key,
            screenName = "Test Screen",
            pattern = ScreenPattern.LIST,
            version = 1,
            template = ScreenTemplate(
                zones = listOf(
                    Zone(
                        id = "zone-1",
                        type = ZoneType.CONTAINER,
                        slots = listOf(
                            Slot(id = "slot-1", controlType = ControlType.LABEL, value = "Hello")
                        )
                    )
                )
            ),
            updatedAt = "2026-01-01T00:00:00Z"
        )
    }

    private fun createFakeRemote(
        screen: ScreenDefinition = createTestScreen(),
        callCounter: MutableList<String> = mutableListOf()
    ): ScreenLoader {
        return object : ScreenLoader {
            override suspend fun loadScreen(
                screenKey: String,
                platform: String?
            ): Result<ScreenDefinition> {
                callCounter.add(screenKey)
                return Result.Success(screen)
            }

            override suspend fun loadNavigation(): Result<NavigationDefinition> {
                return Result.Success(NavigationDefinition())
            }
        }
    }

    private fun createStorage(): SafeEduGoStorage {
        return SafeEduGoStorage.wrap(
            EduGoStorage.withSettings(MapSettings()),
            validateKeys = false
        )
    }

    @Test
    fun loadScreen_returns_from_remote_on_first_call() = runTest {
        val callCounter = mutableListOf<String>()
        val remote = createFakeRemote(callCounter = callCounter)
        val loader = CachedScreenLoader(remote, createStorage())

        val result = loader.loadScreen("test-screen")

        assertTrue(result is Result.Success)
        assertEquals("test-screen", result.data.screenKey)
        assertEquals(1, callCounter.size)
    }

    @Test
    fun loadScreen_returns_from_memory_cache_on_second_call() = runTest {
        val callCounter = mutableListOf<String>()
        val remote = createFakeRemote(callCounter = callCounter)
        val loader = CachedScreenLoader(remote, createStorage())

        loader.loadScreen("test-screen")
        val result = loader.loadScreen("test-screen")

        assertTrue(result is Result.Success)
        assertEquals("test-screen", result.data.screenKey)
        assertEquals(1, callCounter.size)
    }

    @Test
    fun loadScreen_fetches_again_after_cache_expires() = runTest {
        val callCounter = mutableListOf<String>()
        val remote = createFakeRemote(callCounter = callCounter)
        var currentTime = Instant.fromEpochMilliseconds(1000000000L)
        val fakeClock = object : Clock {
            override fun now(): Instant = currentTime
        }

        val loader = CachedScreenLoader(
            remote = remote,
            storage = createStorage(),
            cacheDuration = 30.minutes,
            clock = fakeClock
        )

        loader.loadScreen("test-screen")
        assertEquals(1, callCounter.size)

        currentTime = currentTime + 1.hours
        loader.loadScreen("test-screen")
        assertEquals(2, callCounter.size)
    }

    @Test
    fun evict_removes_screen_from_memory_cache() = runTest {
        val callCounter = mutableListOf<String>()
        val remote = createFakeRemote(callCounter = callCounter)
        val loader = CachedScreenLoader(remote, createStorage())

        loader.loadScreen("test-screen")
        assertEquals(1, callCounter.size)

        loader.evict("test-screen")
        loader.loadScreen("test-screen")
        assertEquals(2, callCounter.size)
    }

    @Test
    fun clearCache_forces_reload_on_next_call() = runTest {
        val callCounter = mutableListOf<String>()
        val remote = createFakeRemote(callCounter = callCounter)
        val loader = CachedScreenLoader(remote, createStorage())

        loader.loadScreen("test-screen")
        loader.clearCache()
        loader.loadScreen("test-screen")

        assertEquals(2, callCounter.size)
    }

    @Test
    fun loadScreen_caches_different_keys_independently() = runTest {
        val callCounter = mutableListOf<String>()
        val remote = object : ScreenLoader {
            override suspend fun loadScreen(
                screenKey: String,
                platform: String?
            ): Result<ScreenDefinition> {
                callCounter.add(screenKey)
                return Result.Success(createTestScreen(screenKey))
            }

            override suspend fun loadNavigation(): Result<NavigationDefinition> {
                return Result.Success(NavigationDefinition())
            }
        }
        val loader = CachedScreenLoader(remote, createStorage())

        loader.loadScreen("screen-a")
        loader.loadScreen("screen-b")
        loader.loadScreen("screen-a")
        loader.loadScreen("screen-b")

        assertEquals(2, callCounter.size)
        assertEquals("screen-a", callCounter[0])
        assertEquals("screen-b", callCounter[1])
    }

    @Test
    fun loadScreen_handles_remote_failure() = runTest {
        val remote = object : ScreenLoader {
            override suspend fun loadScreen(
                screenKey: String,
                platform: String?
            ): Result<ScreenDefinition> {
                return Result.Failure("Network error")
            }

            override suspend fun loadNavigation(): Result<NavigationDefinition> {
                return Result.Failure("Network error")
            }
        }
        val loader = CachedScreenLoader(remote, createStorage())

        val result = loader.loadScreen("test-screen")

        assertTrue(result is Result.Failure)
        assertEquals("Network error", result.error)
    }
}

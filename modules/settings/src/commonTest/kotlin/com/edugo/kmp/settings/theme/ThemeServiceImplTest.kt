package com.edugo.kmp.settings.theme

import com.edugo.kmp.settings.model.ThemeOption
import com.edugo.kmp.storage.EduGoStorage
import com.edugo.kmp.storage.SafeEduGoStorage
import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals

class ThemeServiceImplTest {

    private fun createService(settings: MapSettings = MapSettings()): ThemeServiceImpl {
        val storage = SafeEduGoStorage.wrap(EduGoStorage.withSettings(settings))
        return ThemeServiceImpl(storage)
    }

    @Test
    fun defaultThemeIsSystem() {
        val service = createService()
        assertEquals(ThemeOption.SYSTEM, service.getCurrentTheme())
    }

    @Test
    fun setThemePreferenceUpdatesCurrent() {
        val service = createService()

        service.setThemePreference(ThemeOption.DARK)
        assertEquals(ThemeOption.DARK, service.getCurrentTheme())

        service.setThemePreference(ThemeOption.LIGHT)
        assertEquals(ThemeOption.LIGHT, service.getCurrentTheme())
    }

    @Test
    fun setThemePreferenceUpdatesStateFlow() {
        val service = createService()

        service.setThemePreference(ThemeOption.DARK)
        assertEquals(ThemeOption.DARK, service.themePreference.value)
    }

    @Test
    fun themeIsPersisted() {
        val settings = MapSettings()

        val service1 = createService(settings)
        service1.setThemePreference(ThemeOption.DARK)

        val service2 = createService(settings)
        assertEquals(ThemeOption.DARK, service2.getCurrentTheme())
    }

    @Test
    fun invalidStoredValueFallsBackToDefault() {
        val settings = MapSettings()
        settings.putString("app.theme.preference", "INVALID_VALUE")

        val service = createService(settings)
        assertEquals(ThemeOption.DEFAULT, service.getCurrentTheme())
    }

    @Test
    fun allThemeOptionsCanBeSetAndRetrieved() {
        val settings = MapSettings()

        for (option in ThemeOption.entries) {
            val service = createService(settings)
            service.setThemePreference(option)

            val service2 = createService(settings)
            assertEquals(option, service2.getCurrentTheme())
        }
    }
}

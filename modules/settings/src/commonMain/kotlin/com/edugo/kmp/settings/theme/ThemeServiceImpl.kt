package com.edugo.kmp.settings.theme

import com.edugo.kmp.settings.model.ThemeOption
import com.edugo.kmp.storage.SafeEduGoStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Implementacion de [ThemeService] con persistencia via [SafeEduGoStorage].
 *
 * Lee la preferencia guardada al inicializar y la persiste en cada cambio.
 */
public class ThemeServiceImpl(
    private val storage: SafeEduGoStorage
) : ThemeService {

    private val _themePreference = MutableStateFlow(loadPersistedTheme())

    override val themePreference: StateFlow<ThemeOption> = _themePreference.asStateFlow()

    override fun setThemePreference(option: ThemeOption) {
        storage.putStringSafe(THEME_KEY, option.name)
        _themePreference.value = option
    }

    override fun getCurrentTheme(): ThemeOption = _themePreference.value

    private fun loadPersistedTheme(): ThemeOption {
        val stored = storage.getStringSafe(THEME_KEY, ThemeOption.DEFAULT.name)
        return ThemeOption.fromString(stored)
    }

    private companion object {
        const val THEME_KEY = "app.theme.preference"
    }
}

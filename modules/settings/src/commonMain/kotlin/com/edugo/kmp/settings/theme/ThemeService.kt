package com.edugo.kmp.settings.theme

import com.edugo.kmp.settings.model.ThemeOption
import kotlinx.coroutines.flow.StateFlow

/**
 * Servicio para gestionar la preferencia de tema del usuario.
 *
 * Provee un [StateFlow] reactivo con la preferencia actual
 * y metodos para consultarla y modificarla con persistencia.
 */
public interface ThemeService {
    /**
     * Flow reactivo con la preferencia de tema actual.
     */
    public val themePreference: StateFlow<ThemeOption>

    /**
     * Establece la preferencia de tema y la persiste en storage.
     */
    public fun setThemePreference(option: ThemeOption)

    /**
     * Retorna la preferencia de tema actual.
     */
    public fun getCurrentTheme(): ThemeOption
}

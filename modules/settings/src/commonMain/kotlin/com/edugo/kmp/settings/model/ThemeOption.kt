package com.edugo.kmp.settings.model

/**
 * Opciones de tema disponibles en la aplicacion.
 */
public enum class ThemeOption {
    LIGHT,
    DARK,
    SYSTEM;

    public companion object {
        public val DEFAULT: ThemeOption = SYSTEM

        public fun fromString(value: String): ThemeOption {
            return entries.firstOrNull { it.name == value } ?: DEFAULT
        }
    }
}

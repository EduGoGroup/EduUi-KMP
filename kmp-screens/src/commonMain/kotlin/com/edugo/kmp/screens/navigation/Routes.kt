package com.edugo.kmp.screens.navigation

/**
 * Rutas de navegación de la aplicación.
 *
 * Sealed class que define todas las pantallas navegables.
 * Cada ruta tiene un path único para serialización.
 */
sealed class Route(val path: String) {
    data object Splash : Route("splash")
    data object Login : Route("login")
    data object Home : Route("home")
    data object Settings : Route("settings")

    companion object {
        fun fromPath(path: String): Route? =
            when (path.trim()) {
                "splash" -> Splash
                "login" -> Login
                "home" -> Home
                "settings" -> Settings
                else -> null
            }
    }
}

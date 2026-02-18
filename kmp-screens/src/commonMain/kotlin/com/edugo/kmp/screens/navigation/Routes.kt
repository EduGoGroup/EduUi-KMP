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
    data object Dashboard : Route("dashboard")
    data object MaterialsList : Route("materials-list")
    data class MaterialDetail(val materialId: String) : Route("material-detail/$materialId")

    companion object {
        fun fromPath(path: String): Route? {
            val trimmed = path.trim()
            return when {
                trimmed == "splash" -> Splash
                trimmed == "login" -> Login
                trimmed == "home" -> Home
                trimmed == "settings" -> Settings
                trimmed == "dashboard" -> Dashboard
                trimmed == "materials-list" -> MaterialsList
                trimmed.startsWith("material-detail/") -> {
                    val id = trimmed.removePrefix("material-detail/")
                    if (id.isNotBlank()) MaterialDetail(id) else null
                }
                else -> null
            }
        }

        /**
         * Maps a dynamic-ui screen key to a Route for navigation integration.
         */
        fun fromScreenKey(screenKey: String, params: Map<String, String> = emptyMap()): Route? {
            return when (screenKey) {
                "app-login" -> Login
                "dashboard-home", "dashboard-teacher", "dashboard-student" -> Dashboard
                "materials-list" -> MaterialsList
                "material-detail" -> {
                    val id = params["id"] ?: return null
                    MaterialDetail(id)
                }
                "app-settings" -> Settings
                else -> null
            }
        }
    }
}

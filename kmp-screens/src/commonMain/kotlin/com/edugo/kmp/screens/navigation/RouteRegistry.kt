package com.edugo.kmp.screens.navigation

/**
 * Registry that maps screen keys to Routes.
 * Extends the static Route.fromScreenKey() with dynamic registrations.
 */
class RouteRegistry {
    private val registrations = mutableMapOf<String, (Map<String, String>) -> Route>()

    init {
        // Register all known static routes
        register("splash") { Route.Splash }
        register("app-login") { Route.Login }
        register("dashboard-home") { Route.Dashboard }
        register("dashboard-teacher") { Route.Dashboard }
        register("dashboard-student") { Route.Dashboard }
        register("materials-list") { Route.MaterialsList }
        register("material-detail") { params ->
            Route.MaterialDetail(params["id"] ?: params["materialId"] ?: "")
        }
        register("app-settings") { Route.Settings }
    }

    fun register(screenKey: String, factory: (Map<String, String>) -> Route) {
        registrations[screenKey] = factory
    }

    fun resolve(screenKey: String, params: Map<String, String> = emptyMap()): Route? {
        return registrations[screenKey]?.invoke(params)
    }

    /**
     * For unknown screen keys, creates a Dynamic route that the navigation
     * system can handle by loading the screen dynamically.
     */
    fun resolveOrDynamic(screenKey: String, params: Map<String, String> = emptyMap()): Route {
        return resolve(screenKey, params) ?: Route.Dynamic(screenKey, params)
    }
}

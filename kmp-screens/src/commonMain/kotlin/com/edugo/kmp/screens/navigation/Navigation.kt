package com.edugo.kmp.screens.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

/**
 * Estado de navegacion unificado para todas las plataformas con soporte de backstack.
 *
 * Features:
 * - Backstack funcional: mantiene historial de navegacion
 * - Back navigation: navega hacia atras en el backstack
 * - State persistence: serializacion para restauracion tras background
 */
class NavigationState(private val initialRoute: Route = Route.Splash) {
    private val _backstack: MutableState<List<Route>> = mutableStateOf(listOf(initialRoute))
    private val _currentRoute: MutableState<Route> = mutableStateOf(initialRoute)

    /**
     * Ruta actual (ultima en el backstack) como State observable.
     */
    val currentRoute: Route
        get() = _currentRoute.value

    /**
     * Tamano del backstack.
     */
    val backstackSize: Int
        get() = _backstack.value.size

    /**
     * Backstack completo (solo lectura).
     */
    val backstack: List<Route>
        get() = _backstack.value.toList()

    /**
     * Navega a una nueva ruta agregandola al backstack.
     */
    fun navigateTo(route: Route) {
        _backstack.value = _backstack.value + route
        _currentRoute.value = route
    }

    /**
     * Navega hacia atras eliminando la ruta actual del backstack.
     *
     * @return true si se navego hacia atras, false si ya estamos en el root
     */
    fun back(): Boolean {
        return if (_backstack.value.size > 1) {
            _backstack.value = _backstack.value.dropLast(1)
            _currentRoute.value = _backstack.value.lastOrNull() ?: initialRoute
            true
        } else {
            false
        }
    }

    /**
     * Navega hacia atras hasta una ruta especifica, eliminando todas las rutas
     * intermedias del backstack.
     *
     * @return true si se navego, false si la ruta no existe en el backstack
     */
    fun popTo(route: Route): Boolean {
        val index = _backstack.value.indexOfLast { it.path == route.path }
        return if (index >= 0) {
            _backstack.value = _backstack.value.take(index + 1)
            _currentRoute.value = route
            true
        } else {
            false
        }
    }

    /**
     * Serializa el estado del backstack para restauracion.
     */
    fun saveState(): String {
        return _backstack.value.joinToString(",") { it.path }
    }

    /**
     * Restaura el estado del backstack desde una cadena serializada.
     *
     * @return true si el backstack fue actualizado, false si se ignoro
     */
    fun restoreState(state: String): Boolean {
        if (state.isBlank()) return false

        val routes = state.split(",").mapNotNull { Route.fromPath(it) }

        return if (routes.isNotEmpty()) {
            _backstack.value = routes
            _currentRoute.value = routes.lastOrNull() ?: Route.Splash
            true
        } else {
            false
        }
    }
}

/**
 * Navigator composable para manejo de navegacion multiplataforma.
 */
@Composable
fun NavigationHost(
    navigationState: NavigationState = remember { NavigationState() },
    startRoute: Route = Route.Splash,
    content: @Composable (NavigationState) -> Unit,
) {
    content(navigationState)
}

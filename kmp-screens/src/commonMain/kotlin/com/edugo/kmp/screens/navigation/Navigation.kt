package com.edugo.kmp.screens.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

/**
 * Estado de navegación unificado para todas las plataformas con soporte de backstack.
 *
 * Features:
 * - Backstack funcional: mantiene historial de navegación
 * - Back navigation: navega hacia atrás en el backstack
 * - State persistence: serialización para restauración tras background
 */
class NavigationState(private val initialRoute: Route = Route.Splash) {
    private val _backstack: MutableState<List<Route>> = mutableStateOf(listOf(initialRoute))
    private val _currentRoute: MutableState<Route> = mutableStateOf(initialRoute)

    /**
     * Ruta actual (última en el backstack) como State observable.
     */
    val currentRoute: Route
        get() = _currentRoute.value

    /**
     * Tamaño del backstack.
     */
    val backstackSize: Int
        get() = _backstack.value.size

    /**
     * Backstack completo (solo lectura).
     */
    val backstack: List<Route>
        get() = _backstack.value.toList()

    /**
     * Navega a una nueva ruta agregándola al backstack.
     */
    fun navigateTo(route: Route) {
        _backstack.value += route
        _currentRoute.value = route
    }

    /**
     * Navega hacia atrás eliminando la ruta actual del backstack.
     *
     * @return true si se navego hacia atrás, false si ya estamos en el root
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
     * Reemplaza toda la pila de navegación con una nueva ruta como raíz.
     * Útil para transiciones como login -> dashboard donde no queremos back al login.
     */
    fun replaceAll(route: Route) {
        _backstack.value = listOf(route)
        _currentRoute.value = route
    }

    /**
     * Navega hacia atrás hasta una ruta específica, eliminando todas las rutas
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
     * Serializa el estado del backstack para restauración.
     */
    fun saveState(): String {
        return _backstack.value.joinToString(",") { it.path }
    }

    /**
     * Restaura el estado del backstack desde una cadena serializada.
     *
     * @return true si el backstack fue actualizado, false si se ignoró
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
 * Navigator composable para manejo de navegación multiplataforma.
 */
@Composable
fun NavigationHost(
    navigationState: NavigationState = remember { NavigationState() },
    startRoute: Route = Route.Splash,
    content: @Composable (NavigationState) -> Unit,
) {
    content(navigationState)
}

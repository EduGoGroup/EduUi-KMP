package com.edugo.kmp.screens.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NavigationStateTest {

    @Test
    fun initialRouteIsSplash() {
        val nav = NavigationState()
        assertIs<Route.Splash>(nav.currentRoute)
        assertEquals(1, nav.backstackSize)
    }

    @Test
    fun initialRouteCanBeCustomized() {
        val nav = NavigationState(initialRoute = Route.Login)
        assertIs<Route.Login>(nav.currentRoute)
        assertEquals(1, nav.backstackSize)
    }

    @Test
    fun navigateToAddsToBackstack() {
        val nav = NavigationState()
        nav.navigateTo(Route.Login)

        assertIs<Route.Login>(nav.currentRoute)
        assertEquals(2, nav.backstackSize)
    }

    @Test
    fun navigateToMultipleRoutesBuildsBackstack() {
        val nav = NavigationState()
        nav.navigateTo(Route.Login)
        nav.navigateTo(Route.Home)
        nav.navigateTo(Route.Settings)

        assertIs<Route.Settings>(nav.currentRoute)
        assertEquals(4, nav.backstackSize)
        assertEquals(
            listOf("splash", "login", "home", "settings"),
            nav.backstack.map { it.path }
        )
    }

    @Test
    fun backReturnsTrueAndNavigatesBack() {
        val nav = NavigationState()
        nav.navigateTo(Route.Login)
        nav.navigateTo(Route.Home)

        val result = nav.back()

        assertTrue(result)
        assertIs<Route.Login>(nav.currentRoute)
        assertEquals(2, nav.backstackSize)
    }

    @Test
    fun backReturnsFalseAtRoot() {
        val nav = NavigationState()

        val result = nav.back()

        assertFalse(result)
        assertIs<Route.Splash>(nav.currentRoute)
        assertEquals(1, nav.backstackSize)
    }

    @Test
    fun backMultipleTimesUntilRoot() {
        val nav = NavigationState()
        nav.navigateTo(Route.Login)
        nav.navigateTo(Route.Home)
        nav.navigateTo(Route.Settings)

        assertTrue(nav.back())  // Settings -> Home
        assertIs<Route.Home>(nav.currentRoute)

        assertTrue(nav.back())  // Home -> Login
        assertIs<Route.Login>(nav.currentRoute)

        assertTrue(nav.back())  // Login -> Splash
        assertIs<Route.Splash>(nav.currentRoute)

        assertFalse(nav.back()) // Root - no back
        assertIs<Route.Splash>(nav.currentRoute)
    }

    @Test
    fun popToExistingRouteRemovesIntermediate() {
        val nav = NavigationState()
        nav.navigateTo(Route.Login)
        nav.navigateTo(Route.Home)
        nav.navigateTo(Route.Settings)

        val result = nav.popTo(Route.Login)

        assertTrue(result)
        assertIs<Route.Login>(nav.currentRoute)
        assertEquals(2, nav.backstackSize)
        assertEquals(
            listOf("splash", "login"),
            nav.backstack.map { it.path }
        )
    }

    @Test
    fun popToNonexistentRouteReturnsFalse() {
        val nav = NavigationState()
        nav.navigateTo(Route.Login)

        val result = nav.popTo(Route.Settings)

        assertFalse(result)
        assertIs<Route.Login>(nav.currentRoute)
        assertEquals(2, nav.backstackSize)
    }

    @Test
    fun popToRootRoute() {
        val nav = NavigationState()
        nav.navigateTo(Route.Login)
        nav.navigateTo(Route.Home)

        val result = nav.popTo(Route.Splash)

        assertTrue(result)
        assertIs<Route.Splash>(nav.currentRoute)
        assertEquals(1, nav.backstackSize)
    }

    @Test
    fun saveStateSerializesBackstack() {
        val nav = NavigationState()
        nav.navigateTo(Route.Login)
        nav.navigateTo(Route.Home)

        val state = nav.saveState()

        assertEquals("splash,login,home", state)
    }

    @Test
    fun restoreStateDeserializesBackstack() {
        val nav = NavigationState()

        val result = nav.restoreState("splash,login,home,settings")

        assertTrue(result)
        assertIs<Route.Settings>(nav.currentRoute)
        assertEquals(4, nav.backstackSize)
    }

    @Test
    fun restoreStateWithBlankReturnsFalse() {
        val nav = NavigationState()

        val result = nav.restoreState("")

        assertFalse(result)
        assertIs<Route.Splash>(nav.currentRoute)
    }

    @Test
    fun restoreStateWithInvalidRoutesReturnsFalse() {
        val nav = NavigationState()

        val result = nav.restoreState("invalid,routes,here")

        assertFalse(result)
        assertIs<Route.Splash>(nav.currentRoute)
    }

    @Test
    fun restoreStateIgnoresInvalidRoutes() {
        val nav = NavigationState()

        val result = nav.restoreState("splash,invalid,home")

        assertTrue(result)
        assertEquals(2, nav.backstackSize)
        assertIs<Route.Home>(nav.currentRoute)
    }

    @Test
    fun saveAndRestoreRoundTrip() {
        val nav1 = NavigationState()
        nav1.navigateTo(Route.Login)
        nav1.navigateTo(Route.Home)
        nav1.navigateTo(Route.Settings)

        val state = nav1.saveState()

        val nav2 = NavigationState()
        nav2.restoreState(state)

        assertEquals(nav1.backstackSize, nav2.backstackSize)
        assertEquals(nav1.currentRoute.path, nav2.currentRoute.path)
        assertEquals(
            nav1.backstack.map { it.path },
            nav2.backstack.map { it.path }
        )
    }

    @Test
    fun backstackReturnsDefensiveCopy() {
        val nav = NavigationState()
        nav.navigateTo(Route.Login)

        val backstack = nav.backstack
        assertEquals(2, backstack.size)

        nav.navigateTo(Route.Home)
        assertEquals(2, backstack.size) // Original no cambia
        assertEquals(3, nav.backstackSize)
    }

    @Test
    fun replaceAllClearsPreviousBackstack() {
        val nav = NavigationState()
        nav.navigateTo(Route.Login)
        nav.navigateTo(Route.Home)
        nav.navigateTo(Route.Settings)
        assertEquals(4, nav.backstackSize)

        nav.replaceAll(Route.Dashboard)

        assertIs<Route.Dashboard>(nav.currentRoute)
        assertEquals(1, nav.backstackSize)
        assertEquals(listOf("dashboard"), nav.backstack.map { it.path })
    }

    @Test
    fun replaceAllThenBackReturnsFalse() {
        val nav = NavigationState()
        nav.navigateTo(Route.Login)
        nav.replaceAll(Route.Dashboard)

        val result = nav.back()

        assertFalse(result)
        assertIs<Route.Dashboard>(nav.currentRoute)
    }

    @Test
    fun navigateToDashboardRoute() {
        val nav = NavigationState()
        nav.navigateTo(Route.Dashboard)

        assertIs<Route.Dashboard>(nav.currentRoute)
        assertEquals("dashboard", nav.currentRoute.path)
    }

    @Test
    fun navigateToMaterialsListRoute() {
        val nav = NavigationState()
        nav.navigateTo(Route.MaterialsList)

        assertIs<Route.MaterialsList>(nav.currentRoute)
        assertEquals("materials-list", nav.currentRoute.path)
    }

    @Test
    fun navigateToMaterialDetailRoute() {
        val nav = NavigationState()
        nav.navigateTo(Route.MaterialDetail("abc-123"))

        val route = nav.currentRoute
        assertIs<Route.MaterialDetail>(route)
        assertEquals("abc-123", route.materialId)
        assertEquals("material-detail/abc-123", route.path)
    }

    @Test
    fun materialDetailBackNavigation() {
        val nav = NavigationState()
        nav.navigateTo(Route.Dashboard)
        nav.navigateTo(Route.MaterialsList)
        nav.navigateTo(Route.MaterialDetail("item-1"))

        assertIs<Route.MaterialDetail>(nav.currentRoute)

        assertTrue(nav.back())
        assertIs<Route.MaterialsList>(nav.currentRoute)

        assertTrue(nav.back())
        assertIs<Route.Dashboard>(nav.currentRoute)
    }

    @Test
    fun fullNavigationFlowWithNewRoutes() {
        val nav = NavigationState()

        // Splash -> Login
        nav.replaceAll(Route.Login)
        assertIs<Route.Login>(nav.currentRoute)
        assertEquals(1, nav.backstackSize)

        // Login -> Dashboard (replaceAll)
        nav.replaceAll(Route.Dashboard)
        assertIs<Route.Dashboard>(nav.currentRoute)
        assertEquals(1, nav.backstackSize)

        // Dashboard -> MaterialsList
        nav.navigateTo(Route.MaterialsList)
        assertIs<Route.MaterialsList>(nav.currentRoute)

        // MaterialsList -> MaterialDetail
        nav.navigateTo(Route.MaterialDetail("material-42"))
        assertIs<Route.MaterialDetail>(nav.currentRoute)

        // Back -> MaterialsList
        assertTrue(nav.back())
        assertIs<Route.MaterialsList>(nav.currentRoute)

        // Back -> Dashboard
        assertTrue(nav.back())
        assertIs<Route.Dashboard>(nav.currentRoute)

        // No more back
        assertFalse(nav.back())
    }
}

class RouteTest {

    @Test
    fun fromPathReturnsCorrectRoutes() {
        assertIs<Route.Splash>(Route.fromPath("splash"))
        assertIs<Route.Login>(Route.fromPath("login"))
        assertIs<Route.Home>(Route.fromPath("home"))
        assertIs<Route.Settings>(Route.fromPath("settings"))
    }

    @Test
    fun fromPathReturnsNullForInvalid() {
        assertNull(Route.fromPath("invalid"))
        assertNull(Route.fromPath(""))
    }

    @Test
    fun fromPathTrimsWhitespace() {
        assertIs<Route.Splash>(Route.fromPath("  splash  "))
        assertIs<Route.Login>(Route.fromPath(" login "))
    }

    @Test
    fun routePathsAreUnique() {
        val paths = listOf(
            Route.Splash.path,
            Route.Login.path,
            Route.Home.path,
            Route.Settings.path,
            Route.Dashboard.path,
            Route.MaterialsList.path,
        )
        assertEquals(paths.size, paths.distinct().size)
    }

    @Test
    fun fromPathReturnsDashboardRoute() {
        assertIs<Route.Dashboard>(Route.fromPath("dashboard"))
    }

    @Test
    fun fromPathReturnsMaterialsListRoute() {
        assertIs<Route.MaterialsList>(Route.fromPath("materials-list"))
    }

    @Test
    fun fromPathReturnsMaterialDetailRoute() {
        val route = Route.fromPath("material-detail/abc-123")
        assertIs<Route.MaterialDetail>(route)
        assertEquals("abc-123", route.materialId)
    }

    @Test
    fun fromPathReturnsMaterialDetailWithComplexId() {
        val route = Route.fromPath("material-detail/550e8400-e29b-41d4-a716-446655440000")
        assertIs<Route.MaterialDetail>(route)
        assertEquals("550e8400-e29b-41d4-a716-446655440000", route.materialId)
    }

    @Test
    fun fromPathReturnNullForMaterialDetailWithoutId() {
        assertNull(Route.fromPath("material-detail/"))
    }

    @Test
    fun fromScreenKeyMapsLoginScreenKey() {
        val route = Route.fromScreenKey("app-login")
        assertIs<Route.Login>(route)
    }

    @Test
    fun fromScreenKeyMapsDashboardScreenKeys() {
        assertIs<Route.Dashboard>(Route.fromScreenKey("dashboard-home"))
        assertIs<Route.Dashboard>(Route.fromScreenKey("dashboard-teacher"))
        assertIs<Route.Dashboard>(Route.fromScreenKey("dashboard-student"))
    }

    @Test
    fun fromScreenKeyMapsMaterialsListScreenKey() {
        val route = Route.fromScreenKey("materials-list")
        assertIs<Route.MaterialsList>(route)
    }

    @Test
    fun fromScreenKeyMapsMaterialDetailWithId() {
        val route = Route.fromScreenKey("material-detail", mapOf("id" to "item-42"))
        assertIs<Route.MaterialDetail>(route)
        assertEquals("item-42", route.materialId)
    }

    @Test
    fun fromScreenKeyReturnNullForMaterialDetailWithoutId() {
        assertNull(Route.fromScreenKey("material-detail"))
        assertNull(Route.fromScreenKey("material-detail", emptyMap()))
    }

    @Test
    fun fromScreenKeyMapsSettingsScreenKey() {
        val route = Route.fromScreenKey("app-settings")
        assertIs<Route.Settings>(route)
    }

    @Test
    fun fromScreenKeyReturnNullForUnknown() {
        assertNull(Route.fromScreenKey("unknown-screen"))
        assertNull(Route.fromScreenKey(""))
    }
}

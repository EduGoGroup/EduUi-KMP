package com.edugo.kmp.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.edugo.kmp.auth.model.SchoolInfo
import com.edugo.kmp.auth.service.AuthService
import com.edugo.kmp.auth.service.AuthState
import com.edugo.kmp.design.EduGoTheme
import com.edugo.kmp.di.KoinInitializer
import com.edugo.kmp.dynamicui.sync.DataSyncService
import com.edugo.kmp.screens.dynamic.DynamicScreen
import com.edugo.kmp.screens.dynamic.screens.DynamicLoginScreen
import com.edugo.kmp.screens.dynamic.screens.DynamicMaterialDetailScreen
import com.edugo.kmp.screens.dynamic.screens.MainScreen
import com.edugo.kmp.screens.dynamic.screens.SchoolSelectionScreen
import com.edugo.kmp.screens.navigation.NavigationState
import com.edugo.kmp.screens.navigation.Route
import com.edugo.kmp.screens.navigation.RouteRegistry
import com.edugo.kmp.screens.ui.LoginScreen
import com.edugo.kmp.screens.ui.SplashScreen
import com.edugo.kmp.settings.model.ThemeOption
import com.edugo.kmp.settings.theme.ThemeService
import kotlinx.coroutines.launch
import com.edugo.kmp.screens.di.screenContractsModule
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject

/**
 * Componente principal de la aplicacion compartido entre plataformas.
 *
 * Flujo: Splash -> Login -> [SchoolSelection si >1 escuela] -> Dashboard
 */
@Composable
fun App() {
    KoinApplication(application = {
        modules(KoinInitializer.allModules() + screenContractsModule)
    }) {
        val themeService = koinInject<ThemeService>()
        val themePreference by themeService.themePreference.collectAsState()
        val isSystemDark = isSystemInDarkTheme()

        val darkTheme = when (themePreference) {
            ThemeOption.LIGHT -> false
            ThemeOption.DARK -> true
            ThemeOption.SYSTEM -> isSystemDark
        }

        EduGoTheme(darkTheme = darkTheme) {
            val navState = remember { NavigationState() }
            val routeRegistry = remember { RouteRegistry() }
            val authService = koinInject<AuthService>()
            val dataSyncService = koinInject<DataSyncService>()
            val authState by authService.authState.collectAsState()
            val scope = rememberCoroutineScope()

            // Schools from login response, used by SchoolSelectionScreen
            var pendingSchools by remember { mutableStateOf<List<SchoolInfo>>(emptyList()) }

            // Redirect to login if session expires while app is open
            LaunchedEffect(authState) {
                if (authState is AuthState.Unauthenticated &&
                    navState.currentRoute != Route.Login &&
                    navState.currentRoute != Route.Splash
                ) {
                    dataSyncService.clearAll()
                    navState.replaceAll(Route.Login)
                }
            }

            // Delta sync on token refresh
            LaunchedEffect(Unit) {
                authService.onTokenRefreshed.collect {
                    dataSyncService.deltaSync()
                }
            }

            val handleLogout: () -> Unit = {
                scope.launch {
                    dataSyncService.clearAll()
                    authService.logout()
                }
                navState.replaceAll(Route.Login)
            }

            val handleDynamicNavigate: (String, Map<String, String>) -> Unit = { screenKey, params ->
                val route = routeRegistry.resolveOrDynamic(screenKey, params)
                navState.navigateTo(route)
            }

            when (val currentRoute = navState.currentRoute) {
                Route.Splash -> SplashScreen(
                    onNavigateToLogin = { navState.replaceAll(Route.Login) },
                    onNavigateToHome = { navState.replaceAll(Route.Dashboard) },
                    onNavigateToSchoolSelection = {
                        // User needs to pick a school (e.g. super_admin without schoolId)
                        // Build SchoolInfo list from available contexts in the bundle
                        val bundle = dataSyncService.currentBundle.value
                        val schools = bundle?.availableContexts
                            ?.mapNotNull { ctx ->
                                val sid = ctx.schoolId ?: return@mapNotNull null
                                SchoolInfo(id = sid, name = ctx.schoolName ?: "Escuela")
                            }
                            ?.distinctBy { it.id }
                            ?: emptyList()

                        // Clear stale bundle so menu doesn't bleed into SchoolSelection
                        dataSyncService.clearAll()

                        if (schools.isNotEmpty()) {
                            pendingSchools = schools
                            navState.replaceAll(Route.SchoolSelection)
                        } else {
                            // super_admin with no memberships — go to Dashboard where
                            // SchoolSelectorScreen loads schools from admin API
                            navState.replaceAll(Route.Dashboard)
                        }
                    },
                )

                Route.Login -> LoginScreen(
                    onLoginSuccess = { schools ->
                        if (schools.size > 1) {
                            pendingSchools = schools
                            navState.replaceAll(Route.SchoolSelection)
                        } else {
                            // Single or no school: light sync (menu+perms) then navigate immediately
                            scope.launch {
                                dataSyncService.syncMenuAndPermissions()
                                navState.replaceAll(Route.Dashboard)
                                // Load screens in background - user is already on dashboard
                                dataSyncService.syncScreens()
                            }
                        }
                    }
                )

                Route.SchoolSelection -> SchoolSelectionScreen(
                    schools = pendingSchools,
                    onSyncComplete = { navState.replaceAll(Route.Dashboard) },
                )

                Route.Dashboard -> MainScreen(
                    onNavigate = handleDynamicNavigate,
                    onLogout = handleLogout,
                )

                Route.MaterialsList -> MainScreen(
                    onNavigate = handleDynamicNavigate,
                    onLogout = handleLogout,
                )

                is Route.MaterialDetail -> DynamicMaterialDetailScreen(
                    materialId = currentRoute.materialId,
                    onNavigate = handleDynamicNavigate,
                    onBack = { navState.back() },
                )

                Route.Settings -> MainScreen(
                    onNavigate = handleDynamicNavigate,
                    onLogout = handleLogout,
                )

                Route.Home -> MainScreen(
                    onNavigate = handleDynamicNavigate,
                    onLogout = handleLogout,
                )

                is Route.Dynamic -> {
                    MainScreen(
                        onNavigate = handleDynamicNavigate,
                        onLogout = handleLogout,
                        contentOverride = currentRoute.screenKey,
                        contentParams = currentRoute.params,
                        onBack = { navState.back() },
                    )
                }
            }
        }
    }
}

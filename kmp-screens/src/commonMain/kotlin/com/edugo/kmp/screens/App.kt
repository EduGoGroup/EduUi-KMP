package com.edugo.kmp.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.edugo.kmp.auth.service.AuthService
import com.edugo.kmp.auth.service.AuthState
import com.edugo.kmp.design.EduGoTheme
import com.edugo.kmp.di.KoinInitializer
import com.edugo.kmp.dynamicui.viewmodel.DynamicScreenViewModel
import com.edugo.kmp.screens.dynamic.DynamicScreen
import com.edugo.kmp.screens.dynamic.screens.DynamicLoginScreen
import com.edugo.kmp.screens.dynamic.screens.DynamicMaterialDetailScreen
import com.edugo.kmp.screens.dynamic.screens.MainScreen
import com.edugo.kmp.screens.navigation.NavigationState
import com.edugo.kmp.screens.navigation.Route
import com.edugo.kmp.screens.navigation.RouteRegistry
import com.edugo.kmp.screens.ui.LoginScreen
import com.edugo.kmp.screens.ui.SplashScreen
import com.edugo.kmp.settings.model.ThemeOption
import com.edugo.kmp.settings.theme.ThemeService
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.launch
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject

/**
 * Componente principal de la aplicacion compartido entre plataformas.
 *
 * Inicializa:
 * - Koin con modulos de DI
 * - EduGoTheme (Material 3) con tema reactivo desde ThemeService
 * - Navegacion entre pantallas con RouteRegistry para rutas dinamicas
 *
 * Flujo: Splash -> Login -> Dashboard(MainScreen con BottomNav) -> MaterialDetail
 */
@Composable
fun App() {
    KoinApplication(application = {
        modules(KoinInitializer.allModules())
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
            val authState by authService.authState.collectAsState()
            val scope = rememberCoroutineScope()

            // Redirigir al login si la sesión expira mientras la app está abierta
            LaunchedEffect(authState) {
                if (authState is AuthState.Unauthenticated &&
                    navState.currentRoute != Route.Login &&
                    navState.currentRoute != Route.Splash
                ) {
                    navState.replaceAll(Route.Login)
                }
            }

            val handleLogout: () -> Unit = {
                scope.launch {
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
                    onNavigateToHome = { navState.replaceAll(Route.Dashboard) }
                )

                Route.Login -> LoginScreen(
                    onLoginSuccess = { navState.replaceAll(Route.Dashboard) }
                )

                // TODO: Cambiar a DynamicLoginScreen cuando el endpoint /v1/screens/app-login sea publico
                // Route.Login -> DynamicLoginScreen(
                //     onLoginSuccess = { navState.replaceAll(Route.Dashboard) },
                //     onNavigate = handleDynamicNavigate,
                // )

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

                // Legacy routes - redirect to new flow
                Route.Home -> MainScreen(
                    onNavigate = handleDynamicNavigate,
                    onLogout = handleLogout,
                )

                is Route.Dynamic -> {
                    val viewModel = koinInject<DynamicScreenViewModel>()
                    DynamicScreen(
                        screenKey = currentRoute.screenKey,
                        viewModel = viewModel,
                        onNavigate = handleDynamicNavigate,
                    )
                }
            }
        }
    }
}

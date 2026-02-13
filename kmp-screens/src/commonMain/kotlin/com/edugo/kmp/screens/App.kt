package com.edugo.kmp.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.edugo.kmp.auth.service.AuthService
import com.edugo.kmp.design.EduGoTheme
import com.edugo.kmp.di.KoinInitializer
import com.edugo.kmp.screens.navigation.NavigationState
import com.edugo.kmp.screens.navigation.Route
import com.edugo.kmp.screens.ui.HomeScreen
import com.edugo.kmp.screens.ui.LoginScreen
import com.edugo.kmp.screens.ui.SettingsScreen
import com.edugo.kmp.screens.ui.SplashScreen
import com.edugo.kmp.settings.model.ThemeOption
import com.edugo.kmp.settings.theme.ThemeService
import kotlinx.coroutines.launch
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject

/**
 * Componente principal de la aplicación compartido entre plataformas.
 *
 * Inicializa:
 * - Koin con módulos de DI
 * - EduGoTheme (Material 3) con tema reactivo desde ThemeService
 * - Navegación entre pantallas
 *
 * Flujo: Splash -> Login -> Home -> Settings
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
            val authService = koinInject<AuthService>()
            val scope = rememberCoroutineScope()

            val handleLogout: () -> Unit = {
                scope.launch {
                    authService.logout()
                }
                navState.popTo(Route.Splash)
                navState.navigateTo(Route.Login)
            }

            when (navState.currentRoute) {
                Route.Splash -> SplashScreen(
                    onNavigateToLogin = { navState.navigateTo(Route.Login) },
                    onNavigateToHome = { navState.navigateTo(Route.Home) }
                )

                Route.Login -> LoginScreen(
                    onLoginSuccess = { navState.navigateTo(Route.Home) }
                )

                Route.Home -> HomeScreen(
                    onNavigateToSettings = { navState.navigateTo(Route.Settings) },
                    onLogout = handleLogout
                )

                Route.Settings -> SettingsScreen(
                    onBack = { navState.back() },
                    onLogout = handleLogout
                )
            }
        }
    }
}

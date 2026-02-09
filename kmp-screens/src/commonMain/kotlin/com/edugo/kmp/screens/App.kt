package com.edugo.kmp.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.edugo.kmp.design.EduGoTheme
import com.edugo.kmp.di.KoinInitializer
import com.edugo.kmp.screens.navigation.NavigationState
import com.edugo.kmp.screens.navigation.Route
import com.edugo.kmp.screens.ui.HomeScreen
import com.edugo.kmp.screens.ui.LoginScreen
import com.edugo.kmp.screens.ui.SettingsScreen
import com.edugo.kmp.screens.ui.SplashScreen
import org.koin.compose.KoinApplication

/**
 * Componente principal de la aplicacion compartido entre plataformas.
 *
 * Inicializa:
 * - Koin con modulos de DI
 * - EduGoTheme (Material 3)
 * - Navegacion entre pantallas
 *
 * Flujo: Splash -> Login -> Home -> Settings
 */
@Composable
fun App() {
    KoinApplication(application = {
        modules(KoinInitializer.allModules())
    }) {
        EduGoTheme {
            val navState = remember { NavigationState() }

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
                    onLogout = {
                        navState.popTo(Route.Splash)
                        navState.navigateTo(Route.Login)
                    }
                )

                Route.Settings -> SettingsScreen(
                    onBack = { navState.back() },
                    onLogout = {
                        navState.popTo(Route.Splash)
                        navState.navigateTo(Route.Login)
                    }
                )
            }
        }
    }
}

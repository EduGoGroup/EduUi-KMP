package com.edugo.kmp.screens.dynamic.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.edugo.kmp.design.components.progress.DSLinearProgress
import com.edugo.kmp.dynamicui.loader.ScreenLoader
import com.edugo.kmp.dynamicui.model.NavigationDefinition
import com.edugo.kmp.dynamicui.model.NavigationItem
import com.edugo.kmp.dynamicui.viewmodel.DynamicScreenViewModel
import com.edugo.kmp.foundation.result.Result
import com.edugo.kmp.screens.dynamic.AdaptiveNavigationLayout
import com.edugo.kmp.screens.dynamic.DynamicScreen
import org.koin.compose.koinInject

/**
 * Pantalla principal con navegacion dinamica cargada del backend.
 *
 * Carga la configuracion de navegacion desde el backend y renderiza
 * los tabs correspondientes. Si el backend falla, usa un fallback hardcodeado.
 */
@Composable
fun MainScreen(
    onNavigate: (String, Map<String, String>) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val screenLoader = koinInject<ScreenLoader>()
    var navDefinition by remember { mutableStateOf<NavigationDefinition?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }

    // Load navigation config from backend
    LaunchedEffect(Unit) {
        when (val result = screenLoader.loadNavigation()) {
            is Result.Success -> navDefinition = result.data
            is Result.Failure -> navDefinition = fallbackNavigation()
            is Result.Loading -> { /* already loading */ }
        }
        isLoading = false
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            DSLinearProgress()
        }
        return
    }

    val nav = navDefinition ?: return

    AdaptiveNavigationLayout(
        navDefinition = nav,
        selectedIndex = selectedTab,
        onTabSelected = { selectedTab = it },
        modifier = modifier,
    ) { paddingModifier ->
        // Render current tab's screen
        val currentItem = nav.bottomNav.getOrNull(selectedTab)
        val currentScreenKey = currentItem?.screenKey

        if (currentScreenKey != null) {
            when {
                currentScreenKey.startsWith("dashboard") -> {
                    DynamicDashboardScreen(
                        onNavigate = onNavigate,
                        modifier = paddingModifier,
                    )
                }
                currentScreenKey == "app-settings" -> {
                    DynamicSettingsScreen(
                        onBack = { selectedTab = 0 },
                        onLogout = onLogout,
                        onNavigate = onNavigate,
                        modifier = paddingModifier,
                    )
                }
                else -> {
                    // Generic dynamic screen for any other tab
                    val viewModel = koinInject<DynamicScreenViewModel>()
                    DynamicScreen(
                        screenKey = currentScreenKey,
                        viewModel = viewModel,
                        onNavigate = onNavigate,
                        modifier = paddingModifier,
                    )
                }
            }
        }
    }
}

/** Fallback navigation when backend is unavailable */
private fun fallbackNavigation() = NavigationDefinition(
    bottomNav = listOf(
        NavigationItem(key = "dashboard", label = "Dashboard", icon = "dashboard", screenKey = "dashboard-teacher"),
        NavigationItem(key = "materials", label = "Materials", icon = "folder", screenKey = "materials-list"),
        NavigationItem(key = "settings", label = "Settings", icon = "settings", screenKey = "app-settings"),
    ),
    version = 0,
)

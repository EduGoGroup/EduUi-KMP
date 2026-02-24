package com.edugo.kmp.screens.dynamic.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.edugo.kmp.auth.model.MenuItem
import com.edugo.kmp.auth.repository.MenuRepository
import com.edugo.kmp.auth.service.AuthService
import com.edugo.kmp.auth.service.AuthState
import com.edugo.kmp.design.components.progress.DSLinearProgress
import com.edugo.kmp.dynamicui.model.NavigationDefinition
import com.edugo.kmp.dynamicui.model.NavigationItem
import com.edugo.kmp.dynamicui.viewmodel.DynamicScreenViewModel
import com.edugo.kmp.foundation.result.Result
import com.edugo.kmp.screens.dynamic.AdaptiveNavigationLayout
import com.edugo.kmp.screens.dynamic.DynamicScreen
import com.edugo.kmp.screens.dynamic.UserMenuHeader
import kotlinx.coroutines.launch
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
    val menuRepository = koinInject<MenuRepository>()
    val authService = koinInject<AuthService>()
    val authState by authService.authState.collectAsState()
    val scope = rememberCoroutineScope()

    var navDefinition by remember { mutableStateOf<NavigationDefinition?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var showSchoolSelector by remember { mutableStateOf(false) }

    // Load menu from IAM Platform API
    LaunchedEffect(Unit) {
        when (val result = menuRepository.getMenu()) {
            is Result.Success -> {
                navDefinition = result.data.items.toNavigationDefinition()
                val items = navDefinition!!.drawerItems.ifEmpty { navDefinition!!.bottomNav }
                if (selectedTab >= items.size) selectedTab = 0
            }
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
    // Use drawerItems for expanded, bottomNav for compact (same logic as AdaptiveNavigationLayout)
    val effectiveItems = nav.drawerItems.ifEmpty { nav.bottomNav }

    Box(modifier = modifier) {
    AdaptiveNavigationLayout(
        navDefinition = nav,
        selectedIndex = selectedTab,
        onTabSelected = { selectedTab = it.coerceIn(0, effectiveItems.lastIndex.coerceAtLeast(0)) },
        modifier = Modifier.fillMaxSize(),
        header = {
            val state = authState
            if (state is AuthState.Authenticated) {
                val canSwitchContext = state.activeContext.roleName == "super_admin"
                UserMenuHeader(
                    userName = state.user.getDisplayName(),
                    userRole = state.activeContext.roleName,
                    userInitials = state.user.getInitials(),
                    userEmail = state.user.email,
                    schoolName = state.activeContext.schoolName,
                    onLogout = {
                        scope.launch {
                            authService.logout()
                            onLogout()
                        }
                    },
                    onSwitchContext = if (canSwitchContext) {
                        { showSchoolSelector = true }
                    } else null,
                )
            }
        },
    ) { paddingModifier ->
        // Render current tab's screen
        val currentItem = effectiveItems.getOrNull(selectedTab)

        // Resolve screenKey: use item's own screenKey, or first child's screenKey
        val currentScreenKey = currentItem?.screenKey
            ?: currentItem?.children?.firstOrNull()?.screenKey

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
                    // Check if super_admin needs to select a school first
                    val currentState = authState
                    val needsSchoolSelector = currentState is AuthState.Authenticated
                        && currentState.activeContext.schoolId.isNullOrBlank()

                    if (needsSchoolSelector) {
                        SchoolSelectorScreen(
                            onSchoolSelected = { _, _ ->
                                // Context was switched in SchoolSelectorScreen
                                // authState update triggers recomposition automatically
                            },
                            modifier = paddingModifier,
                        )
                    } else {
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

    // Overlay: school selector when super_admin wants to change school
    if (showSchoolSelector) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopStart,
        ) {
            SchoolSelectorScreen(
                onSchoolSelected = { _, _ ->
                    showSchoolSelector = false
                },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
    } // end outer Box
}

/** Convert menu items from IAM API to NavigationDefinition */
private fun List<MenuItem>.toNavigationDefinition(): NavigationDefinition {
    val navItems = map { it.toNavigationItem() }
    // Items with children go to drawer, flat items to bottom nav
    return if (navItems.any { it.children.isNotEmpty() }) {
        NavigationDefinition(drawerItems = navItems)
    } else {
        NavigationDefinition(bottomNav = navItems)
    }
}

private fun MenuItem.toNavigationItem(): NavigationItem = NavigationItem(
    key = key,
    label = displayName,
    icon = icon,
    screenKey = getDefaultScreen() ?: key,
    sortOrder = sortOrder,
    children = children.map { it.toNavigationItem() },
)

/** Fallback navigation when backend is unavailable */
private fun fallbackNavigation() = NavigationDefinition(
    bottomNav = listOf(
        NavigationItem(key = "dashboard", label = "Dashboard", icon = "dashboard", screenKey = "dashboard"),
        NavigationItem(key = "materials", label = "Materials", icon = "folder", screenKey = "materials-list"),
        NavigationItem(key = "settings", label = "Settings", icon = "settings", screenKey = "app-settings"),
    ),
    version = 0,
)

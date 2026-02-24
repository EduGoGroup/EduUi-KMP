package com.edugo.kmp.screens.dynamic.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.edugo.kmp.dynamicui.model.NavigationItem
import com.edugo.kmp.dynamicui.model.findByKey
import com.edugo.kmp.dynamicui.model.findParentKey
import com.edugo.kmp.dynamicui.model.firstLeaf
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

    var allItems by remember { mutableStateOf<List<NavigationItem>>(emptyList()) }
    var selectedKey by remember { mutableStateOf<String?>(null) }
    var expandedKeys by remember { mutableStateOf<Set<String>>(emptySet()) }
    var isLoading by remember { mutableStateOf(true) }
    var showSchoolSelector by remember { mutableStateOf(false) }

    // Load menu from IAM Platform API
    LaunchedEffect(Unit) {
        when (val result = menuRepository.getMenu()) {
            is Result.Success -> {
                val navItems = result.data.items.map { it.toNavigationItem() }
                allItems = navItems
                // Auto-select first leaf item
                val firstLeaf = navItems.firstLeaf()
                if (firstLeaf != null) {
                    selectedKey = firstLeaf.key
                    // Auto-expand parent of the selected item
                    val parentKey = navItems.findParentKey(firstLeaf.key)
                    if (parentKey != null) {
                        expandedKeys = setOf(parentKey)
                    }
                }
            }
            is Result.Failure -> {
                allItems = fallbackItems()
                selectedKey = allItems.firstLeaf()?.key
            }
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

    if (allItems.isEmpty()) return

    Box(modifier = modifier) {
        AdaptiveNavigationLayout(
            items = allItems,
            selectedKey = selectedKey,
            expandedKeys = expandedKeys,
            onItemSelected = { item ->
                selectedKey = item.key
                // Auto-expand parent if selecting a child
                val parentKey = allItems.findParentKey(item.key)
                if (parentKey != null && parentKey !in expandedKeys) {
                    expandedKeys = expandedKeys + parentKey
                }
            },
            onExpandToggle = { key ->
                expandedKeys = if (key in expandedKeys) {
                    expandedKeys - key
                } else {
                    expandedKeys + key
                }
            },
            modifier = Modifier.fillMaxSize(),
            header = { compact ->
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
                        compact = compact,
                    )
                }
            },
        ) { paddingModifier ->
            // Resolve current screen from selectedKey
            val currentItem = selectedKey?.let { allItems.findByKey(it) }
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
                            onBack = {
                                val firstLeaf = allItems.firstLeaf()
                                if (firstLeaf != null) selectedKey = firstLeaf.key
                            },
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

private fun MenuItem.toNavigationItem(): NavigationItem = NavigationItem(
    key = key,
    label = displayName,
    icon = icon,
    screenKey = getDefaultScreen() ?: key,
    sortOrder = sortOrder,
    children = children.map { it.toNavigationItem() },
)

/** Fallback navigation items when backend is unavailable */
private fun fallbackItems() = listOf(
    NavigationItem(key = "dashboard", label = "Dashboard", icon = "dashboard", screenKey = "dashboard"),
    NavigationItem(key = "materials", label = "Materials", icon = "folder", screenKey = "materials-list"),
    NavigationItem(key = "settings", label = "Settings", icon = "settings", screenKey = "app-settings"),
)

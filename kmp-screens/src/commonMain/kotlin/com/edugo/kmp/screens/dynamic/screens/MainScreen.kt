package com.edugo.kmp.screens.dynamic.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import com.edugo.kmp.auth.model.MenuItem
import com.edugo.kmp.auth.model.UserContext
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
import org.koin.compose.getKoin
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
    contentOverride: String? = null,
    contentParams: Map<String, String> = emptyMap(),
    onBack: (() -> Unit)? = null,
) {
    val menuRepository = koinInject<MenuRepository>()
    val authService = koinInject<AuthService>()
    val authState by authService.authState.collectAsState()
    val scope = rememberCoroutineScope()
    val koin = getKoin()

    var allItems by remember { mutableStateOf<List<NavigationItem>>(emptyList()) }
    var selectedKey by remember { mutableStateOf<String?>(null) }
    var expandedKeys by remember { mutableStateOf<Set<String>>(emptySet()) }
    var isLoading by remember { mutableStateOf(true) }
    var showContextPicker by remember { mutableStateOf(false) }
    var availableContexts by remember { mutableStateOf<List<UserContext>>(emptyList()) }

    // Extract active context for reactive menu loading
    val activeContext = (authState as? AuthState.Authenticated)?.activeContext

    // Reactive: reload menu + contexts whenever active context changes (school/role switch)
    LaunchedEffect(activeContext?.schoolId, activeContext?.roleId) {
        isLoading = true
        // Load available contexts for school switching
        when (val ctxResult = authService.getAvailableContexts()) {
            is Result.Success -> availableContexts = ctxResult.data.available
            else -> { /* keep empty */ }
        }

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
                    expandedKeys += parentKey
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
                    val isSuperAdmin = state.activeContext.hasRole("super_admin")
                    val canSwitch = availableContexts.size > 1 || isSuperAdmin
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
                        onSwitchContext = if (canSwitch) {
                            { showContextPicker = true }
                        } else null,
                        compact = compact,
                    )
                }
            },
        ) { paddingModifier ->
            // If there's a content override (e.g., form/detail from Route.Dynamic), render that
            if (contentOverride != null) {
                val overrideViewModel = remember(contentOverride) { koin.get<DynamicScreenViewModel>() }
                DynamicScreen(
                    screenKey = contentOverride,
                    viewModel = overrideViewModel,
                    placeholders = contentParams,
                    onNavigate = onNavigate,
                    modifier = paddingModifier,
                    onBack = onBack,
                )
                return@AdaptiveNavigationLayout
            }

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
                        // Check if user needs to select a school first
                        val currentState = authState
                        val needsSchoolSelector = currentState is AuthState.Authenticated
                            && currentState.activeContext.schoolId.isNullOrBlank()

                        if (needsSchoolSelector) {
                            SchoolSelectorScreen(
                                onSchoolSelected = { _, _ ->
                                    isLoading = true
                                    // Menu reloads automatically via reactive LaunchedEffect
                                },
                                modifier = paddingModifier,
                            )
                        } else {
                            // Generic dynamic screen for any other tab
                            val viewModel = remember(currentScreenKey) { koin.get<DynamicScreenViewModel>() }
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

        // Overlay: school/context picker when user wants to change school
        if (showContextPicker) {
            val currentState = authState
            val isSuperAdmin = currentState is AuthState.Authenticated
                && currentState.activeContext.hasRole("super_admin")

            if (isSuperAdmin) {
                // Super admin: full school list from admin API (has schools:read)
                DismissibleOverlay(onDismiss = { showContextPicker = false }) {
                    SchoolSelectorScreen(
                        onSchoolSelected = { _, _ ->
                            showContextPicker = false
                            isLoading = true
                            // Menu reloads automatically via reactive LaunchedEffect
                        },
                        modifier = Modifier.heightIn(max = 500.dp),
                    )
                }
            } else {
                // Regular user: context picker from available contexts (no extra perms)
                ContextPickerOverlay(
                    contexts = availableContexts,
                    currentSchoolId = (currentState as? AuthState.Authenticated)?.activeContext?.schoolId,
                    onContextSelected = { context ->
                        showContextPicker = false
                        scope.launch {
                            isLoading = true
                            val schoolId = context.schoolId ?: run {
                                isLoading = false
                                return@launch
                            }
                            when (authService.switchContext(schoolId)) {
                                is Result.Success -> {
                                    // Menu + contexts reload automatically via reactive LaunchedEffect
                                }
                                is Result.Failure -> {
                                    isLoading = false // Context didn't change, reset loading
                                }
                                is Result.Loading -> {}
                            }
                        }
                    },
                    onDismiss = { showContextPicker = false },
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

/** Scrim overlay with a centered dismissible card. */
@Composable
private fun DismissibleOverlay(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth(0.9f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}, // consume click, prevent dismiss
                ),
        ) {
            content()
        }
    }
}

/**
 * Overlay dialog that shows available school contexts for switching.
 * Uses data from getAvailableContexts() — no extra permissions needed.
 */
@Composable
private fun ContextPickerOverlay(
    contexts: List<UserContext>,
    currentSchoolId: String?,
    onContextSelected: (UserContext) -> Unit,
    onDismiss: () -> Unit,
) {
    val otherContexts = contexts.filter { it.schoolId != currentSchoolId }

    // Scrim
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            ),
        contentAlignment = Alignment.Center,
    ) {
        // Card that doesn't propagate clicks to scrim
        Card(
            modifier = Modifier
                .widthIn(max = 400.dp)
                .fillMaxWidth(0.85f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}, // consume click, prevent dismiss
                ),
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Cambiar escuela",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = "Selecciona la escuela a la que deseas cambiar",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(16.dp))

                otherContexts.forEach { ctx ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onContextSelected(ctx) },
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.School,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Column {
                                Text(
                                    text = ctx.schoolName ?: "Escuela",
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Text(
                                    text = ctx.roleName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

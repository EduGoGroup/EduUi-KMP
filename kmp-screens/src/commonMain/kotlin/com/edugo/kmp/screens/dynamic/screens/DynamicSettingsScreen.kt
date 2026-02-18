package com.edugo.kmp.screens.dynamic.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.edugo.kmp.dynamicui.action.ActionResult
import com.edugo.kmp.dynamicui.viewmodel.DynamicScreenViewModel
import com.edugo.kmp.screens.dynamic.DynamicScreen
import com.edugo.kmp.settings.model.ThemeOption
import com.edugo.kmp.settings.theme.ThemeService
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * Settings screen integrada con ThemeService y AuthService via SettingsActionHandler.
 *
 * El SettingsActionHandler registrado en ScreenHandlerRegistry maneja
 * LOGOUT, NAVIGATE_BACK y theme_toggle CONFIRM automaticamente.
 * Este wrapper maneja dark_mode field changes via ThemeService
 * y callbacks de navegacion (onBack, onLogout).
 */
@Composable
fun DynamicSettingsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onNavigate: (String, Map<String, String>) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DynamicScreenViewModel = koinInject(),
) {
    val themeService = koinInject<ThemeService>()

    DynamicScreen(
        screenKey = "app-settings",
        viewModel = viewModel,
        onNavigate = onNavigate,
        modifier = modifier,
        onFieldChanged = { fieldId, value ->
            viewModel.onFieldChanged(fieldId, value)
            if (fieldId == "dark_mode" || fieldId == "preferences.dark_mode") {
                val isDark = value.toBooleanStrictOrNull() ?: false
                themeService.setThemePreference(
                    if (isDark) ThemeOption.DARK else ThemeOption.LIGHT
                )
            }
        },
        onAction = { action, item, scope ->
            scope.launch {
                val result = viewModel.executeAction(action, item)
                when (result) {
                    is ActionResult.NavigateTo -> {
                        if (result.screenKey == "back") {
                            onBack()
                        } else {
                            onNavigate(result.screenKey, result.params)
                        }
                    }
                    is ActionResult.Logout -> onLogout()
                    else -> { /* handled by viewModel */ }
                }
            }
        },
    )
}

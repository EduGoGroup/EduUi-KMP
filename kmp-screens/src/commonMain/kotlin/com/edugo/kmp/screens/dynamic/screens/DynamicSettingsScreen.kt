package com.edugo.kmp.screens.dynamic.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.edugo.kmp.dynamicui.viewmodel.DynamicScreenViewModel
import com.edugo.kmp.screens.dynamic.DynamicScreen
import com.edugo.kmp.settings.model.ThemeOption
import com.edugo.kmp.settings.theme.ThemeService
import org.koin.compose.koinInject

/**
 * Settings screen integrada con ThemeService y AuthService via SettingsContract.
 *
 * El SettingsContract registrado en ScreenContractRegistry maneja
 * LOGOUT y theme_toggle custom events automaticamente.
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
        onNavigate = { screenKey, params ->
            if (screenKey == "back") {
                onBack()
            } else if (screenKey == "logout") {
                onLogout()
            } else {
                onNavigate(screenKey, params)
            }
        },
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
    )
}

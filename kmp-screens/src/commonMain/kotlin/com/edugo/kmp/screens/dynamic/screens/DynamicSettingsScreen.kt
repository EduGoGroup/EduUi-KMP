package com.edugo.kmp.screens.dynamic.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.edugo.kmp.auth.service.AuthService
import com.edugo.kmp.dynamicui.action.ActionResult
import com.edugo.kmp.dynamicui.model.ActionType
import com.edugo.kmp.dynamicui.viewmodel.DynamicScreenViewModel
import com.edugo.kmp.screens.dynamic.DynamicScreen
import com.edugo.kmp.settings.model.ThemeOption
import com.edugo.kmp.settings.theme.ThemeService
import kotlinx.coroutines.launch
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import org.koin.compose.koinInject

/**
 * Settings screen integrada con ThemeService y AuthService.
 *
 * Renderiza la pantalla de configuracion desde el backend (DynamicScreen),
 * pero intercepta acciones especiales:
 * - dark_mode toggle -> ThemeService.setThemePreference()
 * - logout -> AuthService.logout()
 * - NAVIGATE_BACK -> onBack()
 */
@Composable
fun DynamicSettingsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onNavigate: (String, Map<String, String>) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DynamicScreenViewModel = koinInject(),
) {
    val authService = koinInject<AuthService>()
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
            when (action.type) {
                ActionType.NAVIGATE_BACK -> onBack()
                ActionType.CONFIRM -> {
                    val handler = action.config["handler"]?.jsonPrimitive?.contentOrNull
                    if (handler == "theme_toggle") {
                        val current = themeService.getCurrentTheme()
                        themeService.setThemePreference(
                            if (current == ThemeOption.DARK) ThemeOption.LIGHT else ThemeOption.DARK
                        )
                    } else {
                        scope.launch {
                            val result = viewModel.executeAction(action, item)
                            if (result is ActionResult.NavigateTo) {
                                onNavigate(result.screenKey, result.params)
                            }
                        }
                    }
                }
                else -> {
                    scope.launch {
                        val result = viewModel.executeAction(action, item)
                        when (result) {
                            is ActionResult.NavigateTo -> onNavigate(result.screenKey, result.params)
                            is ActionResult.Logout -> {
                                authService.logout()
                                onLogout()
                            }
                            else -> { /* handled by viewModel */ }
                        }
                    }
                }
            }
        },
    )
}

package com.edugo.kmp.screens.dynamic.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.edugo.kmp.auth.service.AuthService
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.components.feedback.DSEmptyState
import com.edugo.kmp.design.components.progress.DSLinearProgress
import com.edugo.kmp.dynamicui.action.ActionResult
import com.edugo.kmp.dynamicui.model.ActionDefinition
import com.edugo.kmp.dynamicui.model.ActionType
import com.edugo.kmp.dynamicui.viewmodel.DynamicScreenViewModel
import com.edugo.kmp.screens.dynamic.renderer.PatternRouter
import com.edugo.kmp.settings.model.ThemeOption
import com.edugo.kmp.settings.theme.ThemeService
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
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
) {
    val authService = koinInject<AuthService>()
    val themeService = koinInject<ThemeService>()
    val viewModel = koinInject<DynamicScreenViewModel>()
    val scope = rememberCoroutineScope()

    val screenState by viewModel.screenState.collectAsState()
    val dataState by viewModel.dataState.collectAsState()
    val fieldValues by viewModel.fieldValues.collectAsState()
    val fieldErrors by viewModel.fieldErrors.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadScreen("app-settings")
    }

    when (val state = screenState) {
        is DynamicScreenViewModel.ScreenState.Loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                DSLinearProgress(modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.spacing8))
            }
        }

        is DynamicScreenViewModel.ScreenState.Ready -> {
            PatternRouter(
                screen = state.screen,
                dataState = dataState,
                fieldValues = fieldValues,
                fieldErrors = fieldErrors,
                onFieldChanged = { fieldId, value ->
                    viewModel.onFieldChanged(fieldId, value)
                    if (fieldId == "dark_mode" || fieldId == "preferences.dark_mode") {
                        val isDark = value.toBooleanStrictOrNull() ?: false
                        themeService.setThemePreference(
                            if (isDark) ThemeOption.DARK else ThemeOption.LIGHT
                        )
                    }
                },
                onAction = { action: ActionDefinition, item: JsonObject? ->
                    when {
                        action.type == ActionType.LOGOUT -> {
                            scope.launch {
                                authService.logout()
                                onLogout()
                            }
                        }
                        action.type == ActionType.NAVIGATE_BACK -> onBack()
                        action.type == ActionType.CONFIRM -> {
                            val handler = action.config["handler"]?.jsonPrimitive?.contentOrNull
                            if (handler == "theme_toggle") {
                                val currentTheme = themeService.getCurrentTheme()
                                val newTheme = if (currentTheme == ThemeOption.DARK) {
                                    ThemeOption.LIGHT
                                } else {
                                    ThemeOption.DARK
                                }
                                themeService.setThemePreference(newTheme)
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
                                    else -> { /* handled by viewModel */ }
                                }
                            }
                        }
                    }
                },
                onNavigate = onNavigate,
                modifier = modifier,
            )
        }

        is DynamicScreenViewModel.ScreenState.Error -> {
            Box(
                modifier = modifier.fillMaxSize().padding(Spacing.spacing4),
                contentAlignment = Alignment.Center,
            ) {
                DSEmptyState(
                    icon = Icons.Default.Warning,
                    title = "Error loading settings",
                    description = state.message,
                    actionLabel = "Retry",
                    onAction = {
                        scope.launch { viewModel.loadScreen("app-settings") }
                    },
                )
            }
        }
    }
}

package com.edugo.kmp.screens.dynamic.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.edugo.kmp.dynamicui.action.ActionResult
import com.edugo.kmp.dynamicui.viewmodel.DynamicScreenViewModel
import com.edugo.kmp.screens.dynamic.DynamicScreen
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * Login screen integrada con AuthService via LoginActionHandler.
 *
 * El LoginActionHandler registrado en ScreenHandlerRegistry maneja
 * SUBMIT_FORM automaticamente, llamando a AuthService.login().
 * Este wrapper solo necesita interceptar la navegacion post-login
 * y mostrar errores de login.
 */
@Composable
fun DynamicLoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigate: (String, Map<String, String>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel = koinInject<DynamicScreenViewModel>()

    DynamicScreen(
        screenKey = "app-login",
        viewModel = viewModel,
        onNavigate = { screenKey, params ->
            if (screenKey == "dashboard-home" || screenKey.startsWith("dashboard")) {
                onLoginSuccess()
            } else {
                onNavigate(screenKey, params)
            }
        },
        modifier = modifier,
        onAction = { action, item, scope ->
            scope.launch {
                val result = viewModel.executeAction(action, item)
                when (result) {
                    is ActionResult.NavigateTo -> {
                        if (result.screenKey == "dashboard-home" || result.screenKey.startsWith("dashboard")) {
                            onLoginSuccess()
                        } else {
                            onNavigate(result.screenKey, result.params)
                        }
                    }
                    is ActionResult.Error -> {
                        viewModel.onFieldChanged("__error", result.message)
                    }
                    else -> { /* handled by viewModel */ }
                }
            }
        },
    )
}

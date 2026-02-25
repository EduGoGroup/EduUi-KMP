package com.edugo.kmp.screens.dynamic.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.edugo.kmp.dynamicui.viewmodel.DynamicScreenViewModel
import com.edugo.kmp.screens.dynamic.DynamicScreen
import org.koin.compose.koinInject

/**
 * Login screen integrada con AuthService via LoginContract.
 *
 * El LoginContract registrado en ScreenContractRegistry maneja
 * SUBMIT_FORM automaticamente, llamando a AuthService.login().
 * Este wrapper solo necesita interceptar la navegacion post-login.
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
    )
}

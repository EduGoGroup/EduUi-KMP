package com.edugo.kmp.screens.dynamic.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.edugo.kmp.auth.model.LoginCredentials
import com.edugo.kmp.auth.model.LoginResult
import com.edugo.kmp.auth.service.AuthService
import com.edugo.kmp.dynamicui.action.ActionResult
import com.edugo.kmp.dynamicui.model.ActionType
import com.edugo.kmp.dynamicui.viewmodel.DynamicScreenViewModel
import com.edugo.kmp.screens.dynamic.DynamicScreen
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * Login screen integrada con AuthService.
 *
 * Renderiza el formulario de login desde la configuracion del backend (DynamicScreen),
 * pero intercepta la accion SUBMIT_FORM para llamar a AuthService.login()
 * en lugar de hacer una llamada HTTP generica.
 */
@Composable
fun DynamicLoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigate: (String, Map<String, String>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val authService = koinInject<AuthService>()
    val viewModel = koinInject<DynamicScreenViewModel>()

    DynamicScreen(
        screenKey = "app-login",
        viewModel = viewModel,
        onNavigate = onNavigate,
        modifier = modifier,
        onAction = { action, _, scope ->
            if (action.type == ActionType.SUBMIT_FORM) {
                scope.launch {
                    val fields = viewModel.fieldValues.value
                    val email = fields["email"] ?: ""
                    val password = fields["password"] ?: ""
                    when (val result = authService.login(LoginCredentials(email, password))) {
                        is LoginResult.Success -> onLoginSuccess()
                        is LoginResult.Error -> viewModel.onFieldChanged(
                            "__error",
                            result.error.getUserFriendlyMessage()
                        )
                    }
                }
            } else {
                scope.launch {
                    val result = viewModel.executeAction(action)
                    if (result is ActionResult.NavigateTo) {
                        onNavigate(result.screenKey, result.params)
                    }
                }
            }
        },
    )
}

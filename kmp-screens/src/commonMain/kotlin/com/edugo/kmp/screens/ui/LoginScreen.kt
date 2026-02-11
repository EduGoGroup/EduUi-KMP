package com.edugo.kmp.screens.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.edugo.kmp.auth.model.LoginCredentials
import com.edugo.kmp.auth.model.LoginResult
import com.edugo.kmp.auth.service.AuthService
import com.edugo.kmp.design.EduGoTheme
import com.edugo.kmp.design.Sizes
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.resources.InitStringsForPreview
import com.edugo.kmp.resources.Strings
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

/**
 * Pantalla de login con campos de email y password.
 *
 * Implementación simplificada sin ViewModel.
 * Válida que los campos no estén vacíos y navega a Home.
 */
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val authService = koinInject<AuthService>()
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val passwordFocusRequester = remember { FocusRequester() }

    fun doLogin() {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = Strings.login_error_empty_fields
            return
        }
        errorMessage = null
        isLoading = true
        scope.launch {
            when (val result = authService.login(LoginCredentials(email, password))) {
                is LoginResult.Success -> onLoginSuccess()
                is LoginResult.Error -> {
                    errorMessage = result.error.getUserFriendlyMessage()
                    isLoading = false
                }
            }
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.l),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = Strings.login_title,
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = Spacing.xl)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(Strings.login_email_label) },
                enabled = !isLoading,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { passwordFocusRequester.requestFocus() }
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.m)
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(Strings.login_password_label) },
                visualTransformation = PasswordVisualTransformation(),
                enabled = !isLoading,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { if (!isLoading) doLogin() }
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.l)
                    .focusRequester(passwordFocusRequester)
            )

            Button(
                onClick = { doLogin() },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(Sizes.progressSmall),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(Strings.login_button)
                }
            }

            errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(Spacing.m))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Preview
@Composable
private fun LoginScreenPreview() {
    InitStringsForPreview()
    EduGoTheme {
        LoginScreen(
            onLoginSuccess = {},
        )
    }
}

@Preview
@Composable
private fun LoginScreenDarkPreview() {
    InitStringsForPreview()
    EduGoTheme(darkTheme = true) {
        LoginScreen(
            onLoginSuccess = {},
        )
    }
}

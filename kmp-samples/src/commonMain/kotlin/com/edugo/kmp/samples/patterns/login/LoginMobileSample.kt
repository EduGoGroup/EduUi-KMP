@file:Suppress("DEPRECATION")
package com.edugo.kmp.samples.patterns.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.components.buttons.DSFilledButton
import com.edugo.kmp.design.components.buttons.DSOutlinedButton
import com.edugo.kmp.design.components.buttons.DSTextButton
import com.edugo.kmp.design.components.inputs.DSOutlinedTextField
import com.edugo.kmp.design.components.inputs.DSPasswordField
import com.edugo.kmp.design.components.media.DSDivider
import com.edugo.kmp.design.components.selection.DSCheckbox
import com.edugo.kmp.samples.DeviceSize
import com.edugo.kmp.samples.SampleDevicePreview
import com.edugo.kmp.samples.SamplePreview
import com.edugo.kmp.samples.data.SampleData

@Composable
fun LoginSampleContent(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    rememberMe: Boolean,
    onRememberMeChange: (Boolean) -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.spacing6, vertical = Spacing.spacing8),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(Spacing.spacing16))

        Text(
            text = "EduGo",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(Modifier.height(Spacing.spacing2))

        Text(
            text = "Bienvenido de vuelta",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(Spacing.spacing8))

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(Spacing.spacing2))
        }

        DSOutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = "Correo electronico",
            placeholder = SampleData.emailPlaceholder,
            leadingIcon = Icons.Default.Email,
            isError = errorMessage != null,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(Spacing.spacing4))

        DSPasswordField(
            value = password,
            onValueChange = onPasswordChange,
            label = "Contrasena",
            placeholder = SampleData.passwordPlaceholder,
            leadingIcon = Icons.Default.Lock,
            isError = errorMessage != null,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(Spacing.spacing2))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DSCheckbox(
                checked = rememberMe,
                onCheckedChange = onRememberMeChange,
                label = "Recordarme",
            )
            DSTextButton(
                text = "Olvide mi contrasena",
                onClick = {},
            )
        }

        Spacer(Modifier.height(Spacing.spacing6))

        DSFilledButton(
            text = "Iniciar Sesion",
            onClick = {},
            loading = isLoading,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(Spacing.spacing6))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DSDivider(modifier = Modifier.weight(1f))
            Text(
                text = "o continuar con",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = Spacing.spacing4),
            )
            DSDivider(modifier = Modifier.weight(1f))
        }

        Spacer(Modifier.height(Spacing.spacing6))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            DSOutlinedButton(text = "Google", onClick = {})
            Spacer(Modifier.width(Spacing.spacing2))
            DSOutlinedButton(text = "Apple", onClick = {})
            Spacer(Modifier.width(Spacing.spacing2))
            DSOutlinedButton(text = "Facebook", onClick = {})
        }

        Spacer(Modifier.height(Spacing.spacing6))

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "No tienes cuenta?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            DSTextButton(
                text = "Registrate",
                onClick = {},
            )
        }
    }
}

// --- Previews ---

@Preview
@Composable
fun LoginMobilePortraitLightPreview() {
    SamplePreview {
        Surface {
            var email by remember { mutableStateOf("") }
            var password by remember { mutableStateOf("") }
            var rememberMe by remember { mutableStateOf(false) }
            LoginSampleContent(
                email = email,
                onEmailChange = { email = it },
                password = password,
                onPasswordChange = { password = it },
                rememberMe = rememberMe,
                onRememberMeChange = { rememberMe = it },
            )
        }
    }
}

@Preview
@Composable
fun LoginMobilePortraitDarkPreview() {
    SamplePreview(darkTheme = true) {
        Surface {
            var email by remember { mutableStateOf(SampleData.emailPlaceholder) }
            var password by remember { mutableStateOf("mipassword123") }
            var rememberMe by remember { mutableStateOf(true) }
            LoginSampleContent(
                email = email,
                onEmailChange = { email = it },
                password = password,
                onPasswordChange = { password = it },
                rememberMe = rememberMe,
                onRememberMeChange = { rememberMe = it },
            )
        }
    }
}

@Preview
@Composable
fun LoginMobileLandscapeLightPreview() {
    SampleDevicePreview(device = DeviceSize.PHONE_LANDSCAPE) {
        Surface(modifier = Modifier.fillMaxSize()) {
            var email by remember { mutableStateOf("") }
            var password by remember { mutableStateOf("") }
            var rememberMe by remember { mutableStateOf(false) }
            LoginSampleContent(
                email = email,
                onEmailChange = { email = it },
                password = password,
                onPasswordChange = { password = it },
                rememberMe = rememberMe,
                onRememberMeChange = { rememberMe = it },
            )
        }
    }
}

@Preview
@Composable
fun LoginMobileLandscapeDarkPreview() {
    SampleDevicePreview(device = DeviceSize.PHONE_LANDSCAPE, darkTheme = true) {
        Surface(modifier = Modifier.fillMaxSize()) {
            var email by remember { mutableStateOf(SampleData.emailPlaceholder) }
            var password by remember { mutableStateOf("mipassword123") }
            var rememberMe by remember { mutableStateOf(true) }
            LoginSampleContent(
                email = email,
                onEmailChange = { email = it },
                password = password,
                onPasswordChange = { password = it },
                rememberMe = rememberMe,
                onRememberMeChange = { rememberMe = it },
            )
        }
    }
}

@file:Suppress("DEPRECATION")
package com.edugo.kmp.samples.patterns.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.text.font.FontWeight
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
import com.edugo.kmp.samples.data.SampleData

@Composable
private fun LoginDesktopSampleContent() {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }

    Row(modifier = Modifier.fillMaxSize()) {
        // LEFT: Branding panel (40%)
        Box(
            modifier = Modifier
                .weight(0.4f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(Spacing.spacing8),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "EduGo",
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(Modifier.height(Spacing.spacing4))

                    Text(
                        text = "Plataforma educativa para el aprendizaje continuo",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )

                    Spacer(Modifier.height(Spacing.spacing2))

                    Text(
                        text = "Accede a cursos, gestiona estudiantes y monitorea el progreso academico desde cualquier lugar.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    )
                }
            }
        }

        // RIGHT: Login form (60%)
        Column(
            modifier = Modifier
                .weight(0.6f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.spacing16, vertical = Spacing.spacing8),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Iniciar Sesion",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(Modifier.height(Spacing.spacing2))

            Text(
                text = "Bienvenido de vuelta",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(Spacing.spacing8))

            DSOutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = "Correo electronico",
                placeholder = SampleData.emailPlaceholder,
                leadingIcon = Icons.Default.Email,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(Spacing.spacing4))

            DSPasswordField(
                value = password,
                onValueChange = { password = it },
                label = "Contrasena",
                placeholder = SampleData.passwordPlaceholder,
                leadingIcon = Icons.Default.Lock,
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
                    onCheckedChange = { rememberMe = it },
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
}

@Preview
@Composable
fun LoginDesktopLightPreview() {
    SampleDevicePreview(device = DeviceSize.DESKTOP) {
        Surface(modifier = Modifier.fillMaxSize()) {
            LoginDesktopSampleContent()
        }
    }
}

@Preview
@Composable
fun LoginDesktopDarkPreview() {
    SampleDevicePreview(device = DeviceSize.DESKTOP, darkTheme = true) {
        Surface(modifier = Modifier.fillMaxSize()) {
            LoginDesktopSampleContent()
        }
    }
}

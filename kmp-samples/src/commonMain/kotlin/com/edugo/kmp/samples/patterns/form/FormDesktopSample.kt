@file:Suppress("DEPRECATION")
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.edugo.kmp.samples.patterns.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.components.buttons.DSFilledButton
import com.edugo.kmp.design.components.inputs.DSOutlinedTextField
import com.edugo.kmp.design.components.media.DSDivider
import com.edugo.kmp.design.components.selection.DSCheckbox
import com.edugo.kmp.samples.DeviceSize
import com.edugo.kmp.samples.SampleDevicePreview
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
private fun FormDesktopContent() {
    var name by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var receiveNotifications by remember { mutableStateOf(false) }
    var acceptTerms by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.spacing6),
    ) {
        Text(
            text = "Registro",
            style = MaterialTheme.typography.headlineMedium,
        )

        Spacer(Modifier.height(Spacing.spacing6))

        // Two columns: personal data (left) + contact (right)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.spacing6),
        ) {
            // Left column: Datos Personales
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Datos Personales",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )

                Spacer(Modifier.height(Spacing.spacing3))

                DSOutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Nombre completo",
                    placeholder = "Ej: Juan Perez",
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(Spacing.spacing3))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.spacing3),
                ) {
                    DSOutlinedTextField(
                        value = dateOfBirth,
                        onValueChange = { dateOfBirth = it },
                        label = "Fecha de nacimiento",
                        placeholder = "DD/MM/AAAA",
                        modifier = Modifier.weight(1f),
                    )
                    DSOutlinedTextField(
                        value = gender,
                        onValueChange = { gender = it },
                        label = "Genero",
                        placeholder = "Seleccionar",
                        readOnly = true,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // Right column: Contacto
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Contacto",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )

                Spacer(Modifier.height(Spacing.spacing3))

                DSOutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Correo electronico",
                    placeholder = "usuario@ejemplo.com",
                    supportingText = "Usaremos este para notificaciones",
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(Spacing.spacing3))

                DSOutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = "Telefono",
                    placeholder = "+57 300 000 0000",
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(Spacing.spacing3))

                DSOutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = "Direccion",
                    placeholder = "Ciudad, Pais",
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        Spacer(Modifier.height(Spacing.spacing6))

        DSDivider()

        Spacer(Modifier.height(Spacing.spacing4))

        // Preferencias - full width
        Text(
            text = "Preferencias",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(Modifier.height(Spacing.spacing2))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.spacing8),
        ) {
            DSCheckbox(
                checked = receiveNotifications,
                onCheckedChange = { receiveNotifications = it },
                label = "Recibir notificaciones",
            )

            DSCheckbox(
                checked = acceptTerms,
                onCheckedChange = { acceptTerms = it },
                label = "Acepto terminos y condiciones",
            )
        }

        Spacer(Modifier.height(Spacing.spacing6))

        DSFilledButton(
            text = "Enviar Registro",
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

// --- Previews ---

@Preview
@Composable
fun FormDesktopLightPreview() {
    SampleDevicePreview(device = DeviceSize.DESKTOP) {
        Surface(modifier = Modifier.fillMaxSize()) {
            FormDesktopContent()
        }
    }
}

@Preview
@Composable
fun FormDesktopDarkPreview() {
    SampleDevicePreview(device = DeviceSize.DESKTOP, darkTheme = true) {
        Surface(modifier = Modifier.fillMaxSize()) {
            FormDesktopContent()
        }
    }
}

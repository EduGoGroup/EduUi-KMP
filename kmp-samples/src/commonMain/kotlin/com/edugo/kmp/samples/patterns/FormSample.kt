@file:Suppress("DEPRECATION")
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.edugo.kmp.samples.patterns

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.edugo.kmp.design.components.buttons.DSIconButton
import com.edugo.kmp.design.components.inputs.DSOutlinedTextField
import com.edugo.kmp.design.components.media.DSDivider
import com.edugo.kmp.design.components.navigation.DSTopAppBar
import com.edugo.kmp.design.components.selection.DSCheckbox
import com.edugo.kmp.samples.SamplePreview
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun FormSampleContent(
    name: String,
    onNameChange: (String) -> Unit,
    dateOfBirth: String,
    onDateOfBirthChange: (String) -> Unit,
    gender: String,
    onGenderChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    receiveNotifications: Boolean,
    onReceiveNotificationsChange: (Boolean) -> Unit,
    acceptTerms: Boolean,
    onAcceptTermsChange: (Boolean) -> Unit,
    nameError: String? = null,
    emailError: String? = null,
    termsError: String? = null,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            DSTopAppBar(
                title = "Registro",
                navigationIcon = {
                    DSIconButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        onClick = {},
                    )
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(horizontal = Spacing.spacing6),
        ) {
            Spacer(Modifier.height(Spacing.spacing4))

            // Seccion: Datos Personales
            Text(
                text = "Datos Personales",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(Modifier.height(Spacing.spacing3))

            DSOutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = "Nombre completo",
                placeholder = "Ej: Juan Perez",
                isError = nameError != null,
                supportingText = nameError,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(Spacing.spacing3))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.spacing3),
            ) {
                DSOutlinedTextField(
                    value = dateOfBirth,
                    onValueChange = onDateOfBirthChange,
                    label = "Fecha de nacimiento",
                    placeholder = "DD/MM/AAAA",
                    modifier = Modifier.weight(1f),
                )
                DSOutlinedTextField(
                    value = gender,
                    onValueChange = onGenderChange,
                    label = "Genero",
                    placeholder = "Seleccionar",
                    readOnly = true,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(Spacing.spacing6))

            DSDivider()

            Spacer(Modifier.height(Spacing.spacing4))

            // Seccion: Contacto
            Text(
                text = "Contacto",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(Modifier.height(Spacing.spacing3))

            DSOutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = "Correo electronico",
                placeholder = "usuario@ejemplo.com",
                isError = emailError != null,
                supportingText = emailError ?: "Usaremos este para notificaciones",
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(Spacing.spacing3))

            DSOutlinedTextField(
                value = phone,
                onValueChange = onPhoneChange,
                label = "Telefono",
                placeholder = "+57 300 000 0000",
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(Spacing.spacing6))

            DSDivider()

            Spacer(Modifier.height(Spacing.spacing4))

            // Seccion: Preferencias
            Text(
                text = "Preferencias",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(Modifier.height(Spacing.spacing2))

            DSCheckbox(
                checked = receiveNotifications,
                onCheckedChange = onReceiveNotificationsChange,
                label = "Recibir notificaciones",
            )

            DSCheckbox(
                checked = acceptTerms,
                onCheckedChange = onAcceptTermsChange,
                label = "Acepto terminos y condiciones",
            )

            if (termsError != null) {
                Text(
                    text = termsError,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = Spacing.spacing12),
                )
            }

            Spacer(Modifier.height(Spacing.spacing6))

            DSFilledButton(
                text = "Enviar",
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(Spacing.spacing6))
        }
    }
}

// --- Previews ---

@Preview
@Composable
fun FormSamplePreview() {
    SamplePreview {
        var name by remember { mutableStateOf("") }
        var dateOfBirth by remember { mutableStateOf("") }
        var gender by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var receiveNotifications by remember { mutableStateOf(false) }
        var acceptTerms by remember { mutableStateOf(false) }
        FormSampleContent(
            name = name,
            onNameChange = { name = it },
            dateOfBirth = dateOfBirth,
            onDateOfBirthChange = { dateOfBirth = it },
            gender = gender,
            onGenderChange = { gender = it },
            email = email,
            onEmailChange = { email = it },
            phone = phone,
            onPhoneChange = { phone = it },
            receiveNotifications = receiveNotifications,
            onReceiveNotificationsChange = { receiveNotifications = it },
            acceptTerms = acceptTerms,
            onAcceptTermsChange = { acceptTerms = it },
        )
    }
}

@Preview
@Composable
fun FormSampleErrorPreview() {
    SamplePreview {
        var name by remember { mutableStateOf("") }
        var dateOfBirth by remember { mutableStateOf("") }
        var gender by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("correo-invalido") }
        var phone by remember { mutableStateOf("") }
        var receiveNotifications by remember { mutableStateOf(true) }
        var acceptTerms by remember { mutableStateOf(false) }
        FormSampleContent(
            name = name,
            onNameChange = { name = it },
            dateOfBirth = dateOfBirth,
            onDateOfBirthChange = { dateOfBirth = it },
            gender = gender,
            onGenderChange = { gender = it },
            email = email,
            onEmailChange = { email = it },
            phone = phone,
            onPhoneChange = { phone = it },
            receiveNotifications = receiveNotifications,
            onReceiveNotificationsChange = { receiveNotifications = it },
            acceptTerms = acceptTerms,
            onAcceptTermsChange = { acceptTerms = it },
            nameError = "El nombre es obligatorio",
            emailError = "Ingresa un correo valido",
            termsError = "Debes aceptar los terminos",
        )
    }
}

@Preview
@Composable
fun FormSampleFilledPreview() {
    SamplePreview(darkTheme = true) {
        var name by remember { mutableStateOf("Juan Perez") }
        var dateOfBirth by remember { mutableStateOf("15/03/1990") }
        var gender by remember { mutableStateOf("Masculino") }
        var email by remember { mutableStateOf("juan.perez@email.com") }
        var phone by remember { mutableStateOf("+57 300 123 4567") }
        var receiveNotifications by remember { mutableStateOf(true) }
        var acceptTerms by remember { mutableStateOf(true) }
        FormSampleContent(
            name = name,
            onNameChange = { name = it },
            dateOfBirth = dateOfBirth,
            onDateOfBirthChange = { dateOfBirth = it },
            gender = gender,
            onGenderChange = { gender = it },
            email = email,
            onEmailChange = { email = it },
            phone = phone,
            onPhoneChange = { phone = it },
            receiveNotifications = receiveNotifications,
            onReceiveNotificationsChange = { receiveNotifications = it },
            acceptTerms = acceptTerms,
            onAcceptTermsChange = { acceptTerms = it },
        )
    }
}

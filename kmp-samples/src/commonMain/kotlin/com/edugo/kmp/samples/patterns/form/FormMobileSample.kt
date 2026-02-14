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
import com.edugo.kmp.samples.DeviceSize
import com.edugo.kmp.samples.SampleDevicePreview
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

@Composable
private fun FormPreviewWrapper(
    darkTheme: Boolean = false,
    name: String = "",
    dateOfBirth: String = "",
    gender: String = "",
    email: String = "",
    phone: String = "",
    receiveNotifications: Boolean = false,
    acceptTerms: Boolean = false,
    nameError: String? = null,
    emailError: String? = null,
    termsError: String? = null,
    content: @Composable (
        name: String, onNameChange: (String) -> Unit,
        dateOfBirth: String, onDateOfBirthChange: (String) -> Unit,
        gender: String, onGenderChange: (String) -> Unit,
        email: String, onEmailChange: (String) -> Unit,
        phone: String, onPhoneChange: (String) -> Unit,
        receiveNotifications: Boolean, onReceiveNotificationsChange: (Boolean) -> Unit,
        acceptTerms: Boolean, onAcceptTermsChange: (Boolean) -> Unit,
        nameError: String?, emailError: String?, termsError: String?,
    ) -> Unit,
) {
    var n by remember { mutableStateOf(name) }
    var dob by remember { mutableStateOf(dateOfBirth) }
    var g by remember { mutableStateOf(gender) }
    var e by remember { mutableStateOf(email) }
    var p by remember { mutableStateOf(phone) }
    var rn by remember { mutableStateOf(receiveNotifications) }
    var at by remember { mutableStateOf(acceptTerms) }
    content(n, { n = it }, dob, { dob = it }, g, { g = it }, e, { e = it }, p, { p = it }, rn, { rn = it }, at, { at = it }, nameError, emailError, termsError)
}

@Preview
@Composable
fun FormPortraitLightPreview() {
    SamplePreview {
        FormPreviewWrapper { name, onName, dob, onDob, gender, onGender, email, onEmail, phone, onPhone, rn, onRn, at, onAt, nameErr, emailErr, termsErr ->
            FormSampleContent(name, onName, dob, onDob, gender, onGender, email, onEmail, phone, onPhone, rn, onRn, at, onAt, nameErr, emailErr, termsErr)
        }
    }
}

@Preview
@Composable
fun FormPortraitDarkPreview() {
    SamplePreview(darkTheme = true) {
        FormPreviewWrapper { name, onName, dob, onDob, gender, onGender, email, onEmail, phone, onPhone, rn, onRn, at, onAt, nameErr, emailErr, termsErr ->
            FormSampleContent(name, onName, dob, onDob, gender, onGender, email, onEmail, phone, onPhone, rn, onRn, at, onAt, nameErr, emailErr, termsErr)
        }
    }
}

@Preview
@Composable
fun FormLandscapeLightPreview() {
    SampleDevicePreview(device = DeviceSize.PHONE_LANDSCAPE) {
        Surface(modifier = Modifier.fillMaxSize()) {
            FormPreviewWrapper { name, onName, dob, onDob, gender, onGender, email, onEmail, phone, onPhone, rn, onRn, at, onAt, nameErr, emailErr, termsErr ->
                FormSampleContent(name, onName, dob, onDob, gender, onGender, email, onEmail, phone, onPhone, rn, onRn, at, onAt, nameErr, emailErr, termsErr)
            }
        }
    }
}

@Preview
@Composable
fun FormLandscapeDarkPreview() {
    SampleDevicePreview(device = DeviceSize.PHONE_LANDSCAPE, darkTheme = true) {
        Surface(modifier = Modifier.fillMaxSize()) {
            FormPreviewWrapper { name, onName, dob, onDob, gender, onGender, email, onEmail, phone, onPhone, rn, onRn, at, onAt, nameErr, emailErr, termsErr ->
                FormSampleContent(name, onName, dob, onDob, gender, onGender, email, onEmail, phone, onPhone, rn, onRn, at, onAt, nameErr, emailErr, termsErr)
            }
        }
    }
}

@Preview
@Composable
fun FormSampleErrorPreview() {
    SamplePreview {
        FormPreviewWrapper(
            email = "correo-invalido",
            receiveNotifications = true,
            nameError = "El nombre es obligatorio",
            emailError = "Ingresa un correo valido",
            termsError = "Debes aceptar los terminos",
        ) { name, onName, dob, onDob, gender, onGender, email, onEmail, phone, onPhone, rn, onRn, at, onAt, nameErr, emailErr, termsErr ->
            FormSampleContent(name, onName, dob, onDob, gender, onGender, email, onEmail, phone, onPhone, rn, onRn, at, onAt, nameErr, emailErr, termsErr)
        }
    }
}

@Preview
@Composable
fun FormSampleFilledPreview() {
    SamplePreview(darkTheme = true) {
        FormPreviewWrapper(
            name = "Juan Perez",
            dateOfBirth = "15/03/1990",
            gender = "Masculino",
            email = "juan.perez@email.com",
            phone = "+57 300 123 4567",
            receiveNotifications = true,
            acceptTerms = true,
        ) { name, onName, dob, onDob, gender, onGender, email, onEmail, phone, onPhone, rn, onRn, at, onAt, nameErr, emailErr, termsErr ->
            FormSampleContent(name, onName, dob, onDob, gender, onGender, email, onEmail, phone, onPhone, rn, onRn, at, onAt, nameErr, emailErr, termsErr)
        }
    }
}

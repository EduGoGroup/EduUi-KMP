@file:Suppress("DEPRECATION")
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.edugo.kmp.samples.patterns.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.edugo.kmp.design.Sizes
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.components.buttons.DSFilledButton
import com.edugo.kmp.design.components.lists.DSListItem
import com.edugo.kmp.design.components.media.DSAvatar
import com.edugo.kmp.design.components.media.DSDivider
import com.edugo.kmp.design.components.media.DSInsetDivider
import com.edugo.kmp.design.components.navigation.DSTopAppBar
import com.edugo.kmp.design.components.selection.DSSwitch
import com.edugo.kmp.samples.DeviceSize
import com.edugo.kmp.samples.SampleDevicePreview
import com.edugo.kmp.samples.SamplePreview
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun SettingsMobileSampleContent() {
    var pushEnabled by remember { mutableStateOf(true) }
    var emailEnabled by remember { mutableStateOf(false) }
    var soundEnabled by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            DSTopAppBar(
                title = "Configuracion",
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            // Cuenta
            Text(
                text = "Cuenta",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(
                    start = Spacing.spacing4,
                    top = Spacing.spacing4,
                    bottom = Spacing.spacing2,
                ),
            )

            DSListItem(
                headlineText = "Juan Perez",
                supportingText = "juan@edugo.com",
                leadingContent = {
                    DSAvatar(
                        initials = "JP",
                        size = Sizes.Avatar.large,
                    )
                },
                trailingContent = {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                onClick = {},
            )

            DSDivider()

            // Apariencia
            Text(
                text = "Apariencia",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(
                    start = Spacing.spacing4,
                    top = Spacing.spacing4,
                    bottom = Spacing.spacing2,
                ),
            )

            DSListItem(
                headlineText = "Tema",
                trailingContent = {
                    Text(
                        text = "Sistema",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                onClick = {},
            )

            DSInsetDivider()

            DSListItem(
                headlineText = "Tamano de texto",
                trailingContent = {
                    Text(
                        text = "Mediano",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                onClick = {},
            )

            DSDivider()

            // Notificaciones
            Text(
                text = "Notificaciones",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(
                    start = Spacing.spacing4,
                    top = Spacing.spacing4,
                    bottom = Spacing.spacing2,
                ),
            )

            DSSwitch(
                checked = pushEnabled,
                onCheckedChange = { pushEnabled = it },
                label = "Notificaciones push",
                modifier = Modifier.padding(horizontal = Spacing.spacing4),
            )

            DSInsetDivider()

            DSSwitch(
                checked = emailEnabled,
                onCheckedChange = { emailEnabled = it },
                label = "Notificaciones por email",
                modifier = Modifier.padding(horizontal = Spacing.spacing4),
            )

            DSInsetDivider()

            DSSwitch(
                checked = soundEnabled,
                onCheckedChange = { soundEnabled = it },
                label = "Sonido",
                modifier = Modifier.padding(horizontal = Spacing.spacing4),
            )

            DSDivider()

            // Navegacion general
            DSListItem(
                headlineText = "Privacidad",
                leadingContent = {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                trailingContent = {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                onClick = {},
            )

            DSInsetDivider()

            DSListItem(
                headlineText = "Idioma",
                supportingText = "Espanol",
                leadingContent = {
                    Icon(
                        Icons.Default.Language,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                trailingContent = {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                onClick = {},
            )

            DSInsetDivider()

            DSListItem(
                headlineText = "Acerca de",
                supportingText = "v1.0.0",
                leadingContent = {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                trailingContent = {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                onClick = {},
            )

            DSDivider()

            Spacer(Modifier.height(Spacing.spacing6))

            DSFilledButton(
                text = "Cerrar Sesion",
                onClick = {},
                leadingIcon = Icons.AutoMirrored.Filled.ExitToApp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.spacing4),
            )

            Spacer(Modifier.height(Spacing.spacing4))
        }
    }
}

// --- Previews ---

@Preview
@Composable
fun SettingsPortraitLightPreview() {
    SamplePreview {
        Surface {
            SettingsMobileSampleContent()
        }
    }
}

@Preview
@Composable
fun SettingsPortraitDarkPreview() {
    SamplePreview(darkTheme = true) {
        Surface {
            SettingsMobileSampleContent()
        }
    }
}

@Preview
@Composable
fun SettingsLandscapeLightPreview() {
    SampleDevicePreview(device = DeviceSize.PHONE_LANDSCAPE) {
        Surface(modifier = Modifier.fillMaxSize()) {
            SettingsMobileSampleContent()
        }
    }
}

@Preview
@Composable
fun SettingsLandscapeDarkPreview() {
    SampleDevicePreview(device = DeviceSize.PHONE_LANDSCAPE, darkTheme = true) {
        Surface(modifier = Modifier.fillMaxSize()) {
            SettingsMobileSampleContent()
        }
    }
}

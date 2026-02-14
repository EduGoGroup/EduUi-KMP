@file:Suppress("DEPRECATION")
package com.edugo.kmp.samples.patterns.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.edugo.kmp.design.Sizes
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.components.lists.DSListItem
import com.edugo.kmp.design.components.media.DSAvatar
import com.edugo.kmp.design.components.media.DSDivider
import com.edugo.kmp.design.components.media.DSInsetDivider
import com.edugo.kmp.design.components.selection.DSSwitch
import com.edugo.kmp.samples.DeviceSize
import com.edugo.kmp.samples.SampleDevicePreview
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
private fun SettingsDesktopContent() {
    var selectedIndex by remember { mutableIntStateOf(0) }

    val categories = listOf(
        "Cuenta" to Icons.Default.Person,
        "Apariencia" to Icons.Default.Palette,
        "Notificaciones" to Icons.Default.Notifications,
        "General" to Icons.Default.Settings,
    )

    Row(modifier = Modifier.fillMaxSize()) {
        // LEFT: Categories list (30%)
        Column(
            modifier = Modifier
                .weight(0.3f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.spacing4),
        ) {
            Text(
                text = "Configuracion",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )

            Spacer(Modifier.height(Spacing.spacing4))

            categories.forEachIndexed { index, (label, icon) ->
                val isSelected = index == selectedIndex
                DSListItem(
                    headlineText = label,
                    leadingContent = {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    },
                    trailingContent = {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    onClick = { selectedIndex = index },
                )
                if (index < categories.size - 1) {
                    DSInsetDivider()
                }
            }
        }

        // RIGHT: Settings content (70%)
        Column(
            modifier = Modifier
                .weight(0.7f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.spacing4),
        ) {
            when (selectedIndex) {
                0 -> AccountSettings()
                1 -> AppearanceSettings()
                2 -> NotificationSettings()
                3 -> GeneralSettings()
            }
        }
    }
}

@Composable
private fun AccountSettings() {
    Text(
        text = "Cuenta",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
    )

    Spacer(Modifier.height(Spacing.spacing4))

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

    DSListItem(
        headlineText = "Editar perfil",
        onClick = {},
    )

    DSInsetDivider()

    DSListItem(
        headlineText = "Cambiar contrasena",
        onClick = {},
    )
}

@Composable
private fun AppearanceSettings() {
    Text(
        text = "Apariencia",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
    )

    Spacer(Modifier.height(Spacing.spacing4))

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
}

@Composable
private fun NotificationSettings() {
    var pushEnabled by remember { mutableStateOf(true) }
    var emailEnabled by remember { mutableStateOf(false) }
    var soundEnabled by remember { mutableStateOf(true) }

    Text(
        text = "Notificaciones",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
    )

    Spacer(Modifier.height(Spacing.spacing4))

    DSSwitch(
        checked = pushEnabled,
        onCheckedChange = { pushEnabled = it },
        label = "Notificaciones push",
        modifier = Modifier.fillMaxWidth(),
    )

    DSInsetDivider()

    DSSwitch(
        checked = emailEnabled,
        onCheckedChange = { emailEnabled = it },
        label = "Notificaciones por email",
        modifier = Modifier.fillMaxWidth(),
    )

    DSInsetDivider()

    DSSwitch(
        checked = soundEnabled,
        onCheckedChange = { soundEnabled = it },
        label = "Sonido",
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun GeneralSettings() {
    Text(
        text = "General",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
    )

    Spacer(Modifier.height(Spacing.spacing4))

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
}

// --- Previews ---

@Preview
@Composable
fun SettingsDesktopLightPreview() {
    SampleDevicePreview(device = DeviceSize.DESKTOP) {
        Surface(modifier = Modifier.fillMaxSize()) {
            SettingsDesktopContent()
        }
    }
}

@Preview
@Composable
fun SettingsDesktopDarkPreview() {
    SampleDevicePreview(device = DeviceSize.DESKTOP, darkTheme = true) {
        Surface(modifier = Modifier.fillMaxSize()) {
            SettingsDesktopContent()
        }
    }
}

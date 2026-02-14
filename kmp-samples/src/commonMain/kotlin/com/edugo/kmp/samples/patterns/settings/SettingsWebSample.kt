@file:Suppress("DEPRECATION")
package com.edugo.kmp.samples.patterns.settings

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.components.cards.DSOutlinedCard
import com.edugo.kmp.design.components.lists.DSListItem
import com.edugo.kmp.design.components.media.DSDivider
import com.edugo.kmp.design.components.media.DSInsetDivider
import com.edugo.kmp.design.components.selection.DSSwitch
import com.edugo.kmp.samples.DeviceSize
import com.edugo.kmp.samples.SampleDevicePreview
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
private fun SettingsWebContent() {
    var pushEnabled by remember { mutableStateOf(true) }
    var emailEnabled by remember { mutableStateOf(false) }
    var soundEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.spacing4),
    ) {
        Text(
            text = "Configuracion Responsiva - Web",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        Spacer(Modifier.height(Spacing.spacing6))

        // Section: Compact (vertical list)
        Text(
            text = "Compact (lista vertical)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        Spacer(Modifier.height(Spacing.spacing2))

        DSOutlinedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(Spacing.spacing2)) {
                Text(
                    text = "Notificaciones",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )

                Spacer(Modifier.height(Spacing.spacing2))

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

                DSDivider()

                Text(
                    text = "General",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = Spacing.spacing3),
                )

                Spacer(Modifier.height(Spacing.spacing2))

                DSListItem(
                    headlineText = "Privacidad",
                    leadingContent = {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    onClick = {},
                )

                DSInsetDivider()

                DSListItem(
                    headlineText = "Idioma",
                    supportingText = "Espanol",
                    leadingContent = {
                        Icon(Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    onClick = {},
                )
            }
        }

        Spacer(Modifier.height(Spacing.spacing8))

        // Section: Expanded (split view)
        Text(
            text = "Expanded (vista dividida)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        Spacer(Modifier.height(Spacing.spacing2))

        DSOutlinedCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(Spacing.spacing2),
                horizontalArrangement = Arrangement.spacedBy(Spacing.spacing4),
            ) {
                // Left: categories
                Column(modifier = Modifier.weight(0.3f)) {
                    Text(
                        text = "Categorias",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )

                    Spacer(Modifier.height(Spacing.spacing2))

                    val categories = listOf(
                        "Cuenta" to Icons.Default.Person,
                        "Apariencia" to Icons.Default.Palette,
                        "Notificaciones" to Icons.Default.Notifications,
                        "General" to Icons.Default.Settings,
                    )

                    categories.forEachIndexed { index, (label, icon) ->
                        DSListItem(
                            headlineText = label,
                            leadingContent = {
                                Icon(
                                    icon,
                                    contentDescription = null,
                                    tint = if (index == 2) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                )
                            },
                            onClick = {},
                        )
                        if (index < categories.size - 1) {
                            DSInsetDivider()
                        }
                    }
                }

                // Right: selected category content
                Column(modifier = Modifier.weight(0.7f)) {
                    Text(
                        text = "Notificaciones",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )

                    Spacer(Modifier.height(Spacing.spacing3))

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
            }
        }
    }
}

// --- Previews ---

@Preview
@Composable
fun SettingsWebDesktopLightPreview() {
    SampleDevicePreview(device = DeviceSize.DESKTOP) {
        Surface(modifier = Modifier.fillMaxSize()) {
            SettingsWebContent()
        }
    }
}

@Preview
@Composable
fun SettingsWebDesktopDarkPreview() {
    SampleDevicePreview(device = DeviceSize.DESKTOP, darkTheme = true) {
        Surface(modifier = Modifier.fillMaxSize()) {
            SettingsWebContent()
        }
    }
}

@file:Suppress("DEPRECATION")
package com.edugo.kmp.samples.patterns.profile

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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.edugo.kmp.design.Sizes
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.components.buttons.DSFilledButton
import com.edugo.kmp.design.components.buttons.DSOutlinedButton
import com.edugo.kmp.design.components.cards.DSElevatedCard
import com.edugo.kmp.design.components.cards.DSOutlinedCard
import com.edugo.kmp.design.components.lists.DSListItem
import com.edugo.kmp.design.components.media.DSAvatar
import com.edugo.kmp.design.components.media.DSDivider
import com.edugo.kmp.samples.DeviceSize
import com.edugo.kmp.samples.SampleDevicePreview
import com.edugo.kmp.samples.data.SampleData

@Composable
private fun ProfileWebSampleContent() {
    val stats = SampleData.profileStats

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.spacing4),
    ) {
        Text(
            text = "Perfil Responsivo - Web",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        Spacer(Modifier.height(Spacing.spacing6))

        // Section: Compact (single column)
        Text(
            text = "Compact (1 columna)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        Spacer(Modifier.height(Spacing.spacing2))

        DSOutlinedCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(Spacing.spacing4),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                DSAvatar(
                    size = Sizes.Avatar.xlarge,
                    initials = SampleData.profileInitials,
                )

                Spacer(Modifier.height(Spacing.spacing3))

                Text(
                    text = SampleData.profileName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = SampleData.profileEmail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(Spacing.spacing3))

                Text(
                    text = SampleData.profileBio,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(Modifier.height(Spacing.spacing4))

                // Stats vertical
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.spacing2),
                ) {
                    stats.forEach { stat ->
                        DSElevatedCard(modifier = Modifier.weight(1f)) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = stat.value,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = stat.label,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(Spacing.spacing4))

                DSFilledButton(
                    text = "Editar Perfil",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(Spacing.spacing2))
                DSOutlinedButton(
                    text = "Compartir Perfil",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        Spacer(Modifier.height(Spacing.spacing8))

        // Section: Expanded (2 columns)
        Text(
            text = "Expanded (2 columnas)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        Spacer(Modifier.height(Spacing.spacing2))

        DSOutlinedCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(Spacing.spacing2),
                horizontalArrangement = Arrangement.spacedBy(Spacing.spacing4),
            ) {
                // Left: Profile card (40%)
                Column(
                    modifier = Modifier
                        .weight(0.4f)
                        .padding(Spacing.spacing4),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    DSAvatar(
                        size = Sizes.Avatar.xxlarge,
                        initials = SampleData.profileInitials,
                    )

                    Spacer(Modifier.height(Spacing.spacing3))

                    Text(
                        text = SampleData.profileName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = SampleData.profileEmail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(Modifier.height(Spacing.spacing3))

                    Text(
                        text = SampleData.profileBio,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(Modifier.height(Spacing.spacing4))

                    DSFilledButton(
                        text = "Editar Perfil",
                        onClick = {},
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(Spacing.spacing2))
                    DSOutlinedButton(
                        text = "Compartir Perfil",
                        onClick = {},
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                // Right: Content (60%)
                Column(
                    modifier = Modifier
                        .weight(0.6f)
                        .padding(Spacing.spacing4),
                ) {
                    // Stats grid (2x2)
                    Text(
                        text = "Estadisticas",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(Spacing.spacing2))

                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.spacing2)) {
                        for (i in stats.indices step 2) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(Spacing.spacing2),
                            ) {
                                DSElevatedCard(modifier = Modifier.weight(1f)) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = stats[i].value,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                        )
                                        Text(
                                            text = stats[i].label,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                        Text(
                                            text = stats[i].change,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                                if (i + 1 < stats.size) {
                                    DSElevatedCard(modifier = Modifier.weight(1f)) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = stats[i + 1].value,
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold,
                                            )
                                            Text(
                                                text = stats[i + 1].label,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                            Text(
                                                text = stats[i + 1].change,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                    }
                                } else {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(Spacing.spacing4))
                    DSDivider()
                    Spacer(Modifier.height(Spacing.spacing4))

                    // Settings shortcuts
                    Text(
                        text = "Configuracion",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(Spacing.spacing2))

                    DSListItem(
                        headlineText = "Configuracion",
                        leadingContent = {
                            Icon(Icons.Default.Settings, contentDescription = null)
                        },
                        onClick = {},
                    )
                    DSDivider()
                    DSListItem(
                        headlineText = "Ayuda",
                        leadingContent = {
                            Icon(Icons.AutoMirrored.Filled.Help, contentDescription = null)
                        },
                        onClick = {},
                    )
                    DSDivider()
                    DSListItem(
                        headlineText = "Cerrar Sesion",
                        leadingContent = {
                            Icon(
                                Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                            )
                        },
                        onClick = {},
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun ProfileWebDesktopLightPreview() {
    SampleDevicePreview(device = DeviceSize.DESKTOP) {
        Surface(modifier = Modifier.fillMaxSize()) {
            ProfileWebSampleContent()
        }
    }
}

@Preview
@Composable
fun ProfileWebDesktopDarkPreview() {
    SampleDevicePreview(device = DeviceSize.DESKTOP, darkTheme = true) {
        Surface(modifier = Modifier.fillMaxSize()) {
            ProfileWebSampleContent()
        }
    }
}

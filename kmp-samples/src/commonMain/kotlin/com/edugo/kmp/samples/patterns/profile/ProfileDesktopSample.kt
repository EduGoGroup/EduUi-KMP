@file:Suppress("DEPRECATION")
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.edugo.kmp.samples.patterns.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
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
import com.edugo.kmp.design.components.lists.DSListItem
import com.edugo.kmp.design.components.media.DSAvatar
import com.edugo.kmp.design.components.media.DSDivider
import com.edugo.kmp.design.components.navigation.DSTopAppBar
import com.edugo.kmp.design.components.navigation.DSTopAppBarVariant
import com.edugo.kmp.samples.DeviceSize
import com.edugo.kmp.samples.SampleDevicePreview
import com.edugo.kmp.samples.data.SampleData

@Composable
private fun ProfileDesktopSampleContent() {
    val stats = SampleData.profileStats
    val activities = listOf(
        Triple("Completo Curso de Algebra Lineal", "hace 2 dias", Icons.Default.School),
        Triple("Obtuvo certificado de Fisica", "hace 1 semana", Icons.Default.WorkspacePremium),
        Triple("Alcanzo racha de 15 dias", "hace 1 semana", Icons.Default.Star),
        Triple("Inicio Curso de Programacion", "hace 2 semanas", Icons.Default.School),
    )

    Row(modifier = Modifier.fillMaxSize()) {
        // LEFT: Profile card (35%)
        Column(
            modifier = Modifier
                .weight(0.35f)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .verticalScroll(rememberScrollState())
                .padding(Spacing.spacing6),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(Spacing.spacing6))

            DSAvatar(
                size = Sizes.Avatar.xxlarge,
                initials = SampleData.profileInitials,
            )

            Spacer(Modifier.height(Spacing.spacing4))

            Text(
                text = SampleData.profileName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(Spacing.spacing1))
            Text(
                text = SampleData.profileEmail,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(Spacing.spacing4))

            Text(
                text = SampleData.profileBio,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(Spacing.spacing6))

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

        // RIGHT: Content (65%)
        Column(
            modifier = Modifier
                .weight(0.65f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
        ) {
            DSTopAppBar(
                title = "Mi Perfil",
                variant = DSTopAppBarVariant.SMALL,
            )

            Column(modifier = Modifier.padding(Spacing.spacing4)) {
                // Stats grid (2x2)
                Text(
                    text = "Estadisticas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )

                Spacer(Modifier.height(Spacing.spacing3))

                Column(verticalArrangement = Arrangement.spacedBy(Spacing.spacing3)) {
                    for (i in stats.indices step 2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.spacing3),
                        ) {
                            DSElevatedCard(modifier = Modifier.weight(1f)) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = stats[i].value,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Spacer(Modifier.height(Spacing.spacing1))
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
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Bold,
                                        )
                                        Spacer(Modifier.height(Spacing.spacing1))
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

                Spacer(Modifier.height(Spacing.spacing6))

                // Activity section
                Text(
                    text = "Actividad Reciente",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )

                Spacer(Modifier.height(Spacing.spacing2))

                activities.forEach { (headline, time, icon) ->
                    DSListItem(
                        headlineText = headline,
                        leadingContent = {
                            Icon(icon, contentDescription = null)
                        },
                        trailingContent = {
                            Text(
                                text = time,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                    )
                    DSDivider()
                }

                Spacer(Modifier.height(Spacing.spacing6))

                // Settings shortcuts
                Text(
                    text = "Configuracion",
                    style = MaterialTheme.typography.titleMedium,
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

@Preview
@Composable
fun ProfileDesktopLightPreview() {
    SampleDevicePreview(device = DeviceSize.DESKTOP) {
        Surface(modifier = Modifier.fillMaxSize()) {
            ProfileDesktopSampleContent()
        }
    }
}

@Preview
@Composable
fun ProfileDesktopDarkPreview() {
    SampleDevicePreview(device = DeviceSize.DESKTOP, darkTheme = true) {
        Surface(modifier = Modifier.fillMaxSize()) {
            ProfileDesktopSampleContent()
        }
    }
}

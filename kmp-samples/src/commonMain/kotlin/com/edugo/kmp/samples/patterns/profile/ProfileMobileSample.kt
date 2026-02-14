@file:Suppress("DEPRECATION")
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.edugo.kmp.samples.SamplePreview
import com.edugo.kmp.samples.data.SampleData

@Composable
private fun ProfileMobileSampleContent() {
    val stats = SampleData.profileStats

    Scaffold(
        topBar = {
            DSTopAppBar(
                title = "Perfil",
                variant = DSTopAppBarVariant.SMALL,
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.spacing4),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(Spacing.spacing6))

            // Hero section: Avatar
            DSAvatar(
                size = Sizes.Avatar.xxlarge,
                initials = SampleData.profileInitials,
            )

            Spacer(Modifier.height(Spacing.spacing4))

            // Name and email
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

            // Bio section
            Text(
                text = SampleData.profileBio,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(Spacing.spacing6))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.spacing2),
            ) {
                stats.forEach { stat ->
                    DSElevatedCard(modifier = Modifier.weight(1f)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stat.value,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(Modifier.height(Spacing.spacing1))
                            Text(
                                text = stat.label,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = stat.change,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(Spacing.spacing6))

            // Action buttons
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

            Spacer(Modifier.height(Spacing.spacing4))
            DSDivider()
            Spacer(Modifier.height(Spacing.spacing4))

            // Settings shortcuts
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

            Spacer(Modifier.height(Spacing.spacing4))
        }
    }
}

@Preview
@Composable
fun ProfileMobilePortraitLightPreview() {
    SamplePreview {
        Surface {
            ProfileMobileSampleContent()
        }
    }
}

@Preview
@Composable
fun ProfileMobilePortraitDarkPreview() {
    SamplePreview(darkTheme = true) {
        Surface {
            ProfileMobileSampleContent()
        }
    }
}

@Preview
@Composable
fun ProfileMobileLandscapeLightPreview() {
    SampleDevicePreview(device = DeviceSize.PHONE_LANDSCAPE) {
        Surface(modifier = Modifier.fillMaxSize()) {
            ProfileMobileSampleContent()
        }
    }
}

@Preview
@Composable
fun ProfileMobileLandscapeDarkPreview() {
    SampleDevicePreview(device = DeviceSize.PHONE_LANDSCAPE, darkTheme = true) {
        Surface(modifier = Modifier.fillMaxSize()) {
            ProfileMobileSampleContent()
        }
    }
}

@file:Suppress("DEPRECATION")
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.edugo.kmp.samples.patterns.dashboard

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.components.buttons.DSFilledButton
import com.edugo.kmp.design.components.buttons.DSOutlinedButton
import com.edugo.kmp.design.components.cards.DSElevatedCard
import com.edugo.kmp.design.components.lists.DSListItem
import com.edugo.kmp.design.components.media.DSDivider
import com.edugo.kmp.design.components.navigation.DSTopAppBar
import com.edugo.kmp.design.components.navigation.DSTopAppBarVariant
import com.edugo.kmp.samples.DeviceSize
import com.edugo.kmp.samples.SampleDevicePreview
import com.edugo.kmp.samples.SamplePreview
import com.edugo.kmp.samples.data.SampleData

@Composable
private fun DashboardMobileSampleContent() {
    val metrics = SampleData.metrics.take(3)
    val activities = listOf(
        Triple("Maria completo Curso de Algebra", "hace 2h", Icons.Default.School),
        Triple("Pedro inicio Curso de Fisica", "hace 5h", Icons.Default.MenuBook),
        Triple("Ana obtuvo certificado de Quimica", "hace 1d", Icons.Default.WorkspacePremium),
        Triple("Luis alcanzo racha de 7 dias", "hace 1d", Icons.Default.Star),
    )

    Scaffold(
        topBar = {
            DSTopAppBar(
                title = "Dashboard",
                variant = DSTopAppBarVariant.SMALL,
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Notifications, contentDescription = null)
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
        ) {
            Spacer(Modifier.height(Spacing.spacing4))

            Text(
                text = "Buenos dias, Usuario",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(Modifier.height(Spacing.spacing4))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.spacing3),
            ) {
                metrics.forEach { metric ->
                    val isPositive = metric.change.startsWith("+")
                    val changeColor = if (isPositive) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                    DSElevatedCard(modifier = Modifier.weight(1f)) {
                        Text(
                            text = metric.value,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(Spacing.spacing1))
                        Text(
                            text = metric.label,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(Spacing.spacing1))
                        Text(
                            text = metric.change,
                            style = MaterialTheme.typography.labelMedium,
                            color = changeColor,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            Spacer(Modifier.height(Spacing.spacing6))

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

            Text(
                text = "Acciones Rapidas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(Modifier.height(Spacing.spacing3))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.spacing2),
            ) {
                DSFilledButton(
                    text = "Nuevo Curso",
                    onClick = {},
                    leadingIcon = Icons.Default.Add,
                )
                DSOutlinedButton(
                    text = "Reportes",
                    onClick = {},
                    leadingIcon = Icons.Default.Assessment,
                )
                DSOutlinedButton(
                    text = "Config",
                    onClick = {},
                    leadingIcon = Icons.Default.Settings,
                )
            }

            Spacer(Modifier.height(Spacing.spacing4))
        }
    }
}

@Preview
@Composable
fun DashboardMobilePortraitLightPreview() {
    SamplePreview {
        Surface {
            DashboardMobileSampleContent()
        }
    }
}

@Preview
@Composable
fun DashboardMobilePortraitDarkPreview() {
    SamplePreview(darkTheme = true) {
        Surface {
            DashboardMobileSampleContent()
        }
    }
}

@Preview
@Composable
fun DashboardMobileLandscapeLightPreview() {
    SampleDevicePreview(device = DeviceSize.PHONE_LANDSCAPE) {
        Surface(modifier = Modifier.fillMaxSize()) {
            DashboardMobileSampleContent()
        }
    }
}

@Preview
@Composable
fun DashboardMobileLandscapeDarkPreview() {
    SampleDevicePreview(device = DeviceSize.PHONE_LANDSCAPE, darkTheme = true) {
        Surface(modifier = Modifier.fillMaxSize()) {
            DashboardMobileSampleContent()
        }
    }
}

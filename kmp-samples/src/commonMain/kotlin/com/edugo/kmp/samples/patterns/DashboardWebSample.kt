@file:Suppress("DEPRECATION")

package com.edugo.kmp.samples.patterns

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
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.components.buttons.DSFilledButton
import com.edugo.kmp.design.components.buttons.DSOutlinedButton
import com.edugo.kmp.design.components.cards.DSElevatedCard
import com.edugo.kmp.design.components.cards.DSOutlinedCard
import com.edugo.kmp.design.components.lists.DSListItem
import com.edugo.kmp.design.components.media.DSDivider
import com.edugo.kmp.samples.DeviceSize
import com.edugo.kmp.samples.SampleDevicePreview
import com.edugo.kmp.samples.data.SampleData
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
private fun DashboardWebSampleContent() {
    val metrics = SampleData.metrics.take(3)
    val activities = listOf(
        Triple("Maria completo Curso de Algebra", "hace 2h", Icons.Default.School),
        Triple("Pedro inicio Curso de Fisica", "hace 5h", Icons.Default.MenuBook),
        Triple("Ana obtuvo certificado de Quimica", "hace 1d", Icons.Default.WorkspacePremium),
        Triple("Luis alcanzo racha de 7 dias", "hace 1d", Icons.Default.Star),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.spacing4),
    ) {
        Text(
            text = "Dashboard Responsivo - Web",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        Spacer(Modifier.height(Spacing.spacing6))

        // Section: Compact (1 column)
        Text(
            text = "Compact (1 columna)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        Spacer(Modifier.height(Spacing.spacing2))

        DSOutlinedCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(Spacing.spacing2),
                verticalArrangement = Arrangement.spacedBy(Spacing.spacing3),
            ) {
                // KPI cards stacked vertically
                metrics.forEach { metric ->
                    val isPositive = metric.change.startsWith("+")
                    val changeColor = if (isPositive) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                    DSElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column {
                                Text(
                                    text = metric.label,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Spacer(Modifier.height(Spacing.spacing1))
                                Text(
                                    text = metric.value,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            Text(
                                text = metric.change,
                                style = MaterialTheme.typography.labelLarge,
                                color = changeColor,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }

                // Activity list below
                Text(
                    text = "Actividad Reciente",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
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
                }
            }
        }

        Spacer(Modifier.height(Spacing.spacing8))

        // Section: Expanded (3 columns)
        Text(
            text = "Expanded (3 columnas)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        Spacer(Modifier.height(Spacing.spacing2))

        DSOutlinedCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(Spacing.spacing2),
            ) {
                // KPI cards in 3 columns
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

                Spacer(Modifier.height(Spacing.spacing4))

                // Two columns: activity (60%) + actions (40%)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.spacing4),
                ) {
                    // Activity list (60%)
                    Column(modifier = Modifier.weight(0.6f)) {
                        Text(
                            text = "Actividad Reciente",
                            style = MaterialTheme.typography.titleSmall,
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
                    }

                    // Quick actions (40%)
                    Column(
                        modifier = Modifier.weight(0.4f),
                        verticalArrangement = Arrangement.spacedBy(Spacing.spacing2),
                    ) {
                        Text(
                            text = "Acciones Rapidas",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(Modifier.height(Spacing.spacing2))
                        DSFilledButton(
                            text = "Nuevo Curso",
                            onClick = {},
                            modifier = Modifier.fillMaxWidth(),
                        )
                        DSOutlinedButton(
                            text = "Ver Reportes",
                            onClick = {},
                            modifier = Modifier.fillMaxWidth(),
                        )
                        DSOutlinedButton(
                            text = "Configuracion",
                            onClick = {},
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun DashboardWebSamplePreview() {
    SampleDevicePreview(device = DeviceSize.DESKTOP) {
        Surface(modifier = Modifier.fillMaxSize()) {
            DashboardWebSampleContent()
        }
    }
}

@Preview
@Composable
fun DashboardWebSampleDarkPreview() {
    SampleDevicePreview(device = DeviceSize.DESKTOP, darkTheme = true) {
        Surface(modifier = Modifier.fillMaxSize()) {
            DashboardWebSampleContent()
        }
    }
}

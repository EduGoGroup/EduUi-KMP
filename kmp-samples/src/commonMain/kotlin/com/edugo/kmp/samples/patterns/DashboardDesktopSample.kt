@file:Suppress("DEPRECATION")

package com.edugo.kmp.samples.patterns

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.components.buttons.DSFilledButton
import com.edugo.kmp.design.components.cards.DSElevatedCard
import com.edugo.kmp.design.components.lists.DSListItem
import com.edugo.kmp.design.components.media.DSDivider
import com.edugo.kmp.design.components.navigation.DSNavigationRail
import com.edugo.kmp.design.components.navigation.DSNavigationRailItem
import com.edugo.kmp.samples.DeviceSize
import com.edugo.kmp.samples.SampleDevicePreview
import com.edugo.kmp.samples.data.SampleData
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
private fun DashboardDesktopSampleContent() {
    var selectedNav by remember { mutableIntStateOf(1) }

    val navItems = listOf(
        DSNavigationRailItem(label = "Inicio", icon = Icons.Default.Home),
        DSNavigationRailItem(label = "Dashboard", icon = Icons.Default.Dashboard),
        DSNavigationRailItem(label = "Usuarios", icon = Icons.Default.People),
        DSNavigationRailItem(label = "Config", icon = Icons.Default.Settings),
    )

    val metrics = SampleData.metrics.take(3)
    val activities = listOf(
        Triple("Maria completo Curso de Algebra", "hace 2h", Icons.Default.School),
        Triple("Pedro inicio Curso de Fisica", "hace 5h", Icons.Default.MenuBook),
        Triple("Ana obtuvo certificado de Quimica", "hace 1d", Icons.Default.WorkspacePremium),
        Triple("Luis alcanzo racha de 7 dias", "hace 1d", Icons.Default.Star),
        Triple("Carlos finalizo evaluacion", "hace 2d", Icons.Default.School),
    )

    Row(modifier = Modifier.fillMaxSize()) {
        // LEFT: Navigation Rail
        DSNavigationRail(
            items = navItems,
            selectedIndex = selectedNav,
            onItemSelected = { selectedNav = it },
            modifier = Modifier.fillMaxHeight(),
        )

        // CENTER: Main content
        Column(
            modifier = Modifier
                .weight(0.6f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.spacing4),
        ) {
            Text(
                text = "Buenos dias, Usuario",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(Modifier.height(Spacing.spacing4))

            // KPI Cards
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

            // Placeholder area for charts
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = MaterialTheme.shapes.medium,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Graficos y tablas",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // RIGHT: Activity panel
        Column(
            modifier = Modifier
                .weight(0.25f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.spacing4),
        ) {
            Text(
                text = "Actividad Reciente",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(Modifier.height(Spacing.spacing2))

            activities.forEach { (headline, time, _) ->
                DSListItem(
                    headlineText = headline,
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

            DSFilledButton(
                text = "Nuevo Curso",
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(Spacing.spacing2))

            DSFilledButton(
                text = "Ver Reportes",
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview
@Composable
fun DashboardDesktopSamplePreview() {
    SampleDevicePreview(device = DeviceSize.DESKTOP) {
        Surface(modifier = Modifier.fillMaxSize()) {
            DashboardDesktopSampleContent()
        }
    }
}

@Preview
@Composable
fun DashboardDesktopSampleDarkPreview() {
    SampleDevicePreview(device = DeviceSize.DESKTOP, darkTheme = true) {
        Surface(modifier = Modifier.fillMaxSize()) {
            DashboardDesktopSampleContent()
        }
    }
}

@file:Suppress("DEPRECATION")
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.edugo.kmp.samples.patterns

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.components.buttons.DSFilledButton
import com.edugo.kmp.design.components.buttons.DSOutlinedButton
import com.edugo.kmp.design.components.navigation.DSDrawerItem
import com.edugo.kmp.design.components.navigation.DSPermanentNavigationDrawer
import com.edugo.kmp.design.components.navigation.DSTopAppBar
import com.edugo.kmp.samples.DeviceSize
import com.edugo.kmp.samples.SampleDevicePreview
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
private fun NavigationDesktopSampleContent() {
    var selectedIndex by remember { mutableIntStateOf(0) }

    val drawerItems = listOf(
        DSDrawerItem(label = "Dashboard", icon = Icons.Default.Dashboard),
        DSDrawerItem(label = "Cursos", icon = Icons.Default.School),
        DSDrawerItem(label = "Estudiantes", icon = Icons.Default.People),
        DSDrawerItem(label = "Reportes", icon = Icons.Default.BarChart),
        DSDrawerItem(label = "Configuracion", icon = Icons.Default.Settings),
    )

    DSPermanentNavigationDrawer(
        items = drawerItems,
        selectedIndex = selectedIndex,
        onItemSelected = { selectedIndex = it },
        modifier = Modifier.fillMaxSize(),
    ) {
        // Main content area
        Column(modifier = Modifier.fillMaxSize()) {
            DSTopAppBar(
                title = drawerItems[selectedIndex].label,
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Spacing.spacing6),
            ) {
                Text(
                    text = "Contenido de ${drawerItems[selectedIndex].label}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(Modifier.height(Spacing.spacing4))

                Text(
                    text = "Esta es el area principal de contenido del layout desktop. " +
                        "El drawer permanente permite navegacion constante sin ocultar el menu.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(Modifier.height(Spacing.spacing6))

                Row {
                    DSFilledButton(
                        text = "Accion principal",
                        onClick = {},
                    )

                    Spacer(Modifier.fillMaxWidth(0.02f))

                    DSOutlinedButton(
                        text = "Accion secundaria",
                        onClick = {},
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun NavigationDesktopSamplePreview() {
    SampleDevicePreview(device = DeviceSize.DESKTOP) {
        Surface(modifier = Modifier.fillMaxSize()) {
            NavigationDesktopSampleContent()
        }
    }
}

@Preview
@Composable
fun NavigationDesktopSampleDarkPreview() {
    SampleDevicePreview(device = DeviceSize.DESKTOP, darkTheme = true) {
        Surface(modifier = Modifier.fillMaxSize()) {
            NavigationDesktopSampleContent()
        }
    }
}

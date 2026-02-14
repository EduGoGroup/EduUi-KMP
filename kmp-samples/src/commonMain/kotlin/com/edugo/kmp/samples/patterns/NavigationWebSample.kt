@file:Suppress("DEPRECATION")

package com.edugo.kmp.samples.patterns

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
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
import com.edugo.kmp.design.components.cards.DSOutlinedCard
import com.edugo.kmp.design.components.navigation.DSBottomNavigationBar
import com.edugo.kmp.design.components.navigation.DSNavigationBarItem
import com.edugo.kmp.design.components.navigation.DSNavigationRail
import com.edugo.kmp.design.components.navigation.DSNavigationRailItem
import com.edugo.kmp.samples.DeviceSize
import com.edugo.kmp.samples.SampleDevicePreview
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
private fun NavigationWebSampleContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.spacing4),
    ) {
        Text(
            text = "Navegacion Responsiva - Web",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        Spacer(Modifier.height(Spacing.spacing4))

        // Section: Mobile (< 600dp)
        SectionLabel("Mobile (< 600dp)")
        DSOutlinedCard(modifier = Modifier.fillMaxWidth()) {
            MobileNavigationDemo()
        }

        Spacer(Modifier.height(Spacing.spacing6))

        // Section: Tablet (600-900dp)
        SectionLabel("Tablet (600-900dp)")
        DSOutlinedCard(modifier = Modifier.fillMaxWidth()) {
            TabletNavigationDemo()
        }

        Spacer(Modifier.height(Spacing.spacing6))

        // Section: Desktop (> 900dp)
        SectionLabel("Desktop (> 900dp)")
        DSOutlinedCard(modifier = Modifier.fillMaxWidth()) {
            DesktopNavigationDemo()
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = Spacing.spacing2),
    )
}

@Composable
private fun MobileNavigationDemo() {
    var selectedIndex by remember { mutableIntStateOf(0) }

    val items = listOf(
        DSNavigationBarItem(label = "Inicio", icon = Icons.Default.Home),
        DSNavigationBarItem(label = "Cursos", icon = Icons.Default.Star),
        DSNavigationBarItem(label = "Buscar", icon = Icons.Default.Search),
        DSNavigationBarItem(label = "Perfil", icon = Icons.Default.Person),
    )

    Scaffold(
        bottomBar = {
            DSBottomNavigationBar(
                items = items,
                selectedIndex = selectedIndex,
                onItemSelected = { selectedIndex = it },
            )
        },
        modifier = Modifier.height(300.dp),
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Contenido: ${items[selectedIndex].label}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TabletNavigationDemo() {
    var selectedIndex by remember { mutableIntStateOf(0) }

    val items = listOf(
        DSNavigationRailItem(label = "Inicio", icon = Icons.Default.Home),
        DSNavigationRailItem(label = "Cursos", icon = Icons.Default.Star),
        DSNavigationRailItem(label = "Buscar", icon = Icons.Default.Search),
        DSNavigationRailItem(label = "Perfil", icon = Icons.Default.Person),
    )

    Row(modifier = Modifier.height(300.dp).fillMaxWidth()) {
        DSNavigationRail(
            items = items,
            selectedIndex = selectedIndex,
            onItemSelected = { selectedIndex = it },
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Contenido: ${items[selectedIndex].label}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DesktopNavigationDemo() {
    var selectedIndex by remember { mutableIntStateOf(0) }

    val drawerLabels = listOf("Inicio", "Cursos", "Buscar", "Perfil", "Configuracion")
    val drawerIcons = listOf(
        Icons.Default.Home,
        Icons.Default.Star,
        Icons.Default.Search,
        Icons.Default.Person,
        Icons.Default.Settings,
    )

    Row(modifier = Modifier.height(300.dp).fillMaxWidth()) {
        // Drawer simulation
        Column(
            modifier = Modifier
                .width(280.dp)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(vertical = Spacing.spacing2),
        ) {
            Text(
                text = "EduGo",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(Spacing.spacing4),
            )

            drawerLabels.forEachIndexed { index, label ->
                NavigationDrawerItem(
                    label = { Text(label) },
                    selected = selectedIndex == index,
                    onClick = { selectedIndex = index },
                    icon = {
                        Icon(drawerIcons[index], contentDescription = label)
                    },
                    modifier = Modifier.padding(horizontal = Spacing.spacing2),
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Contenido: ${drawerLabels[selectedIndex]}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview
@Composable
fun NavigationWebSamplePreview() {
    SampleDevicePreview(device = DeviceSize.DESKTOP) {
        Surface(modifier = Modifier.fillMaxSize()) {
            NavigationWebSampleContent()
        }
    }
}

@Preview
@Composable
fun NavigationWebSampleDarkPreview() {
    SampleDevicePreview(device = DeviceSize.DESKTOP, darkTheme = true) {
        Surface(modifier = Modifier.fillMaxSize()) {
            NavigationWebSampleContent()
        }
    }
}

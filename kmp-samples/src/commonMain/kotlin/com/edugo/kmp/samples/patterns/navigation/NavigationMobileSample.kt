@file:Suppress("DEPRECATION")
package com.edugo.kmp.samples.patterns.navigation

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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.components.cards.DSOutlinedCard
import com.edugo.kmp.design.components.media.DSBadge
import com.edugo.kmp.design.components.navigation.DSBottomNavigationBar
import com.edugo.kmp.design.components.navigation.DSNavigationBarItem
import com.edugo.kmp.design.components.navigation.DSNavigationRail
import com.edugo.kmp.design.components.navigation.DSNavigationRailItem
import com.edugo.kmp.design.components.navigation.DSTabItem
import com.edugo.kmp.design.components.navigation.DSTabs
import com.edugo.kmp.design.components.navigation.DSTabVariant
import com.edugo.kmp.samples.DeviceSize
import com.edugo.kmp.samples.SampleDevicePreview
import com.edugo.kmp.samples.SamplePreview

@Composable
fun NavigationMobileSampleContent(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.spacing4),
    ) {
        // Section: Bottom Navigation
        Text(
            text = "Bottom Navigation",
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(Spacing.spacing2))
        BottomNavigationSection()

        Spacer(Modifier.height(Spacing.spacing6))

        // Section: Tabs
        Text(
            text = "Tabs",
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(Spacing.spacing2))
        TabsSection()

        Spacer(Modifier.height(Spacing.spacing6))

        // Section: Navigation Rail
        Text(
            text = "Navigation Rail",
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(Spacing.spacing2))
        NavigationRailSection()

        Spacer(Modifier.height(Spacing.spacing6))

        // Section: Navigation Drawer (inline simulation)
        Text(
            text = "Navigation Drawer",
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(Spacing.spacing2))
        NavigationDrawerSection()
    }
}

@Composable
private fun BottomNavigationSection() {
    var selectedIndex by remember { mutableIntStateOf(0) }

    val items = listOf(
        DSNavigationBarItem(label = "Inicio", icon = Icons.Default.Home),
        DSNavigationBarItem(label = "Cursos", icon = Icons.Default.Star),
        DSNavigationBarItem(label = "Perfil", icon = Icons.Default.Person),
        DSNavigationBarItem(label = "Config", icon = Icons.Default.Settings),
    )

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Contenido de la pagina",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        DSBottomNavigationBar(
            items = items,
            selectedIndex = selectedIndex,
            onItemSelected = { selectedIndex = it },
        )
    }
}

@Composable
private fun TabsSection() {
    var selectedTab by remember { mutableIntStateOf(0) }

    val tabs = listOf(
        DSTabItem(label = "Todos"),
        DSTabItem(label = "Recientes"),
        DSTabItem(label = "Favoritos"),
        DSTabItem(label = "Archivados"),
    )

    Column {
        DSTabs(
            tabs = tabs,
            selectedIndex = selectedTab,
            onTabSelected = { selectedTab = it },
            variant = DSTabVariant.PRIMARY,
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Contenido de: ${tabs[selectedTab].label}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun NavigationRailSection() {
    var selectedIndex by remember { mutableIntStateOf(0) }

    val items = listOf(
        DSNavigationRailItem(label = "Inicio", icon = Icons.Default.Home),
        DSNavigationRailItem(label = "Cursos", icon = Icons.Default.Star),
        DSNavigationRailItem(label = "Buscar", icon = Icons.Default.Search),
        DSNavigationRailItem(label = "Perfil", icon = Icons.Default.Person),
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp),
    ) {
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
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun NavigationDrawerSection() {
    var selectedIndex by remember { mutableIntStateOf(0) }

    val drawerItems = listOf(
        Triple("Inicio", Icons.Default.Home, false),
        Triple("Cursos", Icons.Default.Star, true),
        Triple("Buscar", Icons.Default.Search, false),
        Triple("Configuracion", Icons.Default.Settings, false),
    )

    DSOutlinedCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(vertical = Spacing.spacing2),
        ) {
            drawerItems.forEachIndexed { index, (label, icon, hasBadge) ->
                NavigationDrawerItem(
                    label = { Text(label) },
                    selected = selectedIndex == index,
                    onClick = { selectedIndex = index },
                    icon = {
                        if (hasBadge) {
                            DSBadge(count = 3) {
                                Icon(icon, contentDescription = label)
                            }
                        } else {
                            Icon(icon, contentDescription = label)
                        }
                    },
                    modifier = Modifier.padding(horizontal = Spacing.spacing2),
                )
            }
        }
    }
}

// --- Previews ---

@Preview
@Composable
fun NavigationMobilePortraitLightPreview() {
    SamplePreview {
        Surface {
            NavigationMobileSampleContent()
        }
    }
}

@Preview
@Composable
fun NavigationMobilePortraitDarkPreview() {
    SamplePreview(darkTheme = true) {
        Surface {
            NavigationMobileSampleContent()
        }
    }
}

@Preview
@Composable
fun NavigationMobileLandscapeLightPreview() {
    SampleDevicePreview(device = DeviceSize.PHONE_LANDSCAPE) {
        Surface(modifier = Modifier.fillMaxSize()) {
            NavigationMobileSampleContent()
        }
    }
}

@Preview
@Composable
fun NavigationMobileLandscapeDarkPreview() {
    SampleDevicePreview(device = DeviceSize.PHONE_LANDSCAPE, darkTheme = true) {
        Surface(modifier = Modifier.fillMaxSize()) {
            NavigationMobileSampleContent()
        }
    }
}

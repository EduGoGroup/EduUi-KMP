@file:Suppress("DEPRECATION")

package com.edugo.kmp.samples.catalog

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.components.navigation.DSBottomNavigationBar
import com.edugo.kmp.design.components.navigation.DSNavigationBarItem
import com.edugo.kmp.design.components.navigation.DSNavigationRail
import com.edugo.kmp.design.components.navigation.DSNavigationRailItem
import com.edugo.kmp.design.components.navigation.DSTabItem
import com.edugo.kmp.design.components.navigation.DSTabVariant
import com.edugo.kmp.design.components.navigation.DSTabs
import com.edugo.kmp.design.components.navigation.DSTopAppBar
import com.edugo.kmp.design.components.navigation.DSTopAppBarVariant
import com.edugo.kmp.samples.SamplePreview
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationCatalog() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.spacing4),
    ) {
        Text("Navigation Catalog", style = MaterialTheme.typography.headlineSmall)

        // --- DSTopAppBar ---
        Spacer(Modifier.height(Spacing.spacing6))
        Text("DSTopAppBar", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(Spacing.spacing4))
        Text("SMALL", style = MaterialTheme.typography.labelMedium)
        DSTopAppBar(
            title = "Small Top Bar",
            variant = DSTopAppBarVariant.SMALL,
            navigationIcon = {
                IconButton(onClick = {}) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = {}) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More")
                }
            },
        )

        Spacer(Modifier.height(Spacing.spacing4))
        Text("CENTER_ALIGNED", style = MaterialTheme.typography.labelMedium)
        DSTopAppBar(
            title = "Center Aligned",
            variant = DSTopAppBarVariant.CENTER_ALIGNED,
            navigationIcon = {
                IconButton(onClick = {}) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
        )

        Spacer(Modifier.height(Spacing.spacing4))
        Text("MEDIUM", style = MaterialTheme.typography.labelMedium)
        DSTopAppBar(
            title = "Medium Top Bar",
            variant = DSTopAppBarVariant.MEDIUM,
            navigationIcon = {
                IconButton(onClick = {}) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
        )

        Spacer(Modifier.height(Spacing.spacing4))
        Text("LARGE", style = MaterialTheme.typography.labelMedium)
        DSTopAppBar(
            title = "Large Top Bar",
            variant = DSTopAppBarVariant.LARGE,
            navigationIcon = {
                IconButton(onClick = {}) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
        )

        // --- DSBottomNavigationBar ---
        Spacer(Modifier.height(Spacing.spacing6))
        Text("DSBottomNavigationBar", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(Spacing.spacing4))
        Text("With 4 items", style = MaterialTheme.typography.labelMedium)
        var bottomNavSelected by remember { mutableIntStateOf(0) }
        DSBottomNavigationBar(
            items = listOf(
                DSNavigationBarItem("Inicio", Icons.Default.Home),
                DSNavigationBarItem("Buscar", Icons.Default.Search),
                DSNavigationBarItem("Favoritos", Icons.Default.Favorite),
                DSNavigationBarItem("Perfil", Icons.Default.Person),
            ),
            selectedIndex = bottomNavSelected,
            onItemSelected = { bottomNavSelected = it },
        )

        // --- DSTabs ---
        Spacer(Modifier.height(Spacing.spacing6))
        Text("DSTabs", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(Spacing.spacing4))
        Text("PRIMARY", style = MaterialTheme.typography.labelMedium)
        var primaryTabSelected by remember { mutableIntStateOf(0) }
        DSTabs(
            tabs = listOf(
                DSTabItem("Cursos"),
                DSTabItem("Actividad"),
                DSTabItem("Favoritos"),
            ),
            selectedIndex = primaryTabSelected,
            onTabSelected = { primaryTabSelected = it },
            variant = DSTabVariant.PRIMARY,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(Spacing.spacing4))
        Text("SECONDARY", style = MaterialTheme.typography.labelMedium)
        var secondaryTabSelected by remember { mutableIntStateOf(1) }
        DSTabs(
            tabs = listOf(
                DSTabItem("Todos"),
                DSTabItem("Recientes"),
                DSTabItem("Populares"),
            ),
            selectedIndex = secondaryTabSelected,
            onTabSelected = { secondaryTabSelected = it },
            variant = DSTabVariant.SECONDARY,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(Spacing.spacing4))
        Text("With icons", style = MaterialTheme.typography.labelMedium)
        var iconTabSelected by remember { mutableIntStateOf(0) }
        DSTabs(
            tabs = listOf(
                DSTabItem("Inicio", Icons.Default.Home),
                DSTabItem("Buscar", Icons.Default.Search),
                DSTabItem("Perfil", Icons.Default.Person),
            ),
            selectedIndex = iconTabSelected,
            onTabSelected = { iconTabSelected = it },
            modifier = Modifier.fillMaxWidth(),
        )

        // --- DSNavigationRail ---
        Spacer(Modifier.height(Spacing.spacing6))
        Text("DSNavigationRail", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(Spacing.spacing4))
        Text("With 4 items", style = MaterialTheme.typography.labelMedium)
        var railSelected by remember { mutableIntStateOf(0) }
        Row(modifier = Modifier.height(300.dp)) {
            DSNavigationRail(
                items = listOf(
                    DSNavigationRailItem("Inicio", Icons.Default.Home),
                    DSNavigationRailItem("Buscar", Icons.Default.Search),
                    DSNavigationRailItem("Favoritos", Icons.Default.Favorite),
                    DSNavigationRailItem("Config", Icons.Default.Settings),
                ),
                selectedIndex = railSelected,
                onItemSelected = { railSelected = it },
            )
            Spacer(Modifier.width(Spacing.spacing4))
            Text(
                "Contenido del area principal",
                modifier = Modifier.padding(Spacing.spacing4),
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        Spacer(Modifier.height(Spacing.spacing6))
    }
}

@Preview
@Composable
fun NavigationCatalogPreview() {
    SamplePreview {
        Surface {
            NavigationCatalog()
        }
    }
}

@Preview
@Composable
fun NavigationCatalogDarkPreview() {
    SamplePreview(darkTheme = true) {
        Surface {
            NavigationCatalog()
        }
    }
}

package com.edugo.kmp.screens.dynamic

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.material3.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.edugo.kmp.design.components.navigation.DSBottomNavigationBar
import com.edugo.kmp.design.components.navigation.DSNavigationBarItem
import com.edugo.kmp.design.components.navigation.DSNavigationRail
import com.edugo.kmp.design.components.navigation.DSNavigationRailItem
import com.edugo.kmp.dynamicui.model.NavigationDefinition
import com.edugo.kmp.dynamicui.model.NavigationItem

/**
 * Adaptive navigation layout that renders:
 * - Bottom nav for compact/medium screens (< 840dp)
 * - NavigationRail for expanded screens (>= 840dp)
 */
@Composable
fun AdaptiveNavigationLayout(
    navDefinition: NavigationDefinition,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    header: @Composable (ColumnScope.() -> Unit)? = null,
    content: @Composable (Modifier) -> Unit
) {
    // Use drawerItems for expanded layout, bottomNav for compact
    // If drawerItems is available (desktop/web), prefer it for the rail
    val effectiveItems = navDefinition.drawerItems.ifEmpty { navDefinition.bottomNav }

    val navBarItems = navDefinition.bottomNav.ifEmpty { effectiveItems }.map { item ->
        DSNavigationBarItem(
            label = item.label,
            icon = resolveIcon(item.icon, filled = false),
            selectedIcon = resolveIcon(item.icon, filled = true),
        )
    }

    val railItems = effectiveItems.map { item ->
        DSNavigationRailItem(
            label = item.label,
            icon = resolveIcon(item.icon, filled = false),
            selectedIcon = resolveIcon(item.icon, filled = true),
        )
    }

    androidx.compose.foundation.layout.BoxWithConstraints(modifier = modifier) {
        val windowWidth = maxWidth

        when {
            windowWidth >= 840.dp -> {
                // EXPANDED: NavigationRail + content
                Row(modifier = Modifier.fillMaxSize()) {
                    DSNavigationRail(
                        items = railItems,
                        selectedIndex = selectedIndex,
                        onItemSelected = onTabSelected,
                        header = header,
                    )
                    Surface(modifier = Modifier.weight(1f)) {
                        content(Modifier.fillMaxSize())
                    }
                }
            }
            else -> {
                // COMPACT/MEDIUM: Bottom Navigation Bar
                Scaffold(
                    topBar = {
                        if (header != null) {
                            Surface(
                                tonalElevation = 2.dp,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column { header() }
                                }
                            }
                        }
                    },
                    bottomBar = {
                        if (navBarItems.isNotEmpty()) {
                            DSBottomNavigationBar(
                                items = navBarItems,
                                selectedIndex = selectedIndex,
                                onItemSelected = onTabSelected,
                            )
                        }
                    },
                ) { paddingValues ->
                    content(Modifier.padding(paddingValues))
                }
            }
        }
    }
}

/**
 * Maps icon name strings from backend to Material Icons.
 */
private fun resolveIcon(iconName: String?, filled: Boolean): ImageVector {
    if (iconName == null) return if (filled) Icons.Filled.Info else Icons.Outlined.Info

    return when (iconName.lowercase()) {
        "home" -> if (filled) Icons.Filled.Home else Icons.Outlined.Home
        "dashboard" -> if (filled) Icons.Filled.Dashboard else Icons.Outlined.Dashboard
        "folder", "materials" -> if (filled) Icons.Filled.Folder else Icons.Outlined.FolderOpen
        "settings", "gear" -> if (filled) Icons.Filled.Settings else Icons.Outlined.Settings
        "person", "profile" -> if (filled) Icons.Filled.Person else Icons.Outlined.Person
        "users", "people", "group" -> if (filled) Icons.Filled.Group else Icons.Outlined.Group
        "school" -> if (filled) Icons.Filled.School else Icons.Outlined.School
        "shield" -> if (filled) Icons.Filled.Shield else Icons.Outlined.Shield
        "key" -> if (filled) Icons.Filled.Key else Icons.Outlined.Key
        "graduation-cap", "graduation_cap" -> if (filled) Icons.Filled.School else Icons.Outlined.School
        "book-open", "book_open", "book" -> if (filled) Icons.Filled.Book else Icons.Outlined.Book
        "layers" -> if (filled) Icons.Filled.Layers else Icons.Outlined.Layers
        "user-plus", "user_plus" -> if (filled) Icons.Filled.PersonAdd else Icons.Outlined.PersonAdd
        "file-text", "file_text" -> if (filled) Icons.Filled.Description else Icons.Outlined.Description
        "clipboard", "assessment" -> if (filled) Icons.Filled.Assessment else Icons.Outlined.Assessment
        "bar-chart", "bar_chart" -> if (filled) Icons.Filled.BarChart else Icons.Outlined.BarChart
        "trending-up", "trending_up" -> if (filled) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Outlined.TrendingUp
        "pie-chart", "pie_chart" -> if (filled) Icons.Filled.PieChart else Icons.Outlined.PieChart
        else -> if (filled) Icons.Filled.Info else Icons.Outlined.Info
    }
}

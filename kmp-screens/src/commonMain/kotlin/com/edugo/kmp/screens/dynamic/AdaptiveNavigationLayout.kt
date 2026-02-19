package com.edugo.kmp.screens.dynamic

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.edugo.kmp.design.components.navigation.DSBottomNavigationBar
import com.edugo.kmp.design.components.navigation.DSNavigationBarItem
import com.edugo.kmp.dynamicui.model.NavigationDefinition
import com.edugo.kmp.dynamicui.model.NavigationItem

/**
 * Adaptive navigation layout that renders bottom nav from NavigationDefinition.
 * Phase 2: Bottom nav for all platforms.
 * Phase 3 will add NavigationRail (tablet) and PermanentNavigationDrawer (desktop).
 */
@Composable
fun AdaptiveNavigationLayout(
    navDefinition: NavigationDefinition,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (Modifier) -> Unit
) {
    val navBarItems = navDefinition.bottomNav.map { item ->
        DSNavigationBarItem(
            label = item.label,
            icon = resolveIcon(item.icon, filled = false),
            selectedIcon = resolveIcon(item.icon, filled = true),
        )
    }

    Scaffold(
        modifier = modifier,
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
        else -> if (filled) Icons.Filled.Info else Icons.Outlined.Info
    }
}

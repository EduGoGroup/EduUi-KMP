package com.edugo.kmp.screens.dynamic

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.Icon
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.edugo.kmp.design.components.navigation.DSBottomNavigationBar
import com.edugo.kmp.design.components.navigation.DSDrawerChild
import com.edugo.kmp.design.components.navigation.DSDrawerSection
import com.edugo.kmp.design.components.navigation.DSExpandableDrawerContent
import com.edugo.kmp.design.components.navigation.DSNavigationBarItem
import com.edugo.kmp.design.components.navigation.DSNavigationRail
import com.edugo.kmp.design.components.navigation.DSNavigationRailItem
import com.edugo.kmp.design.tokens.Breakpoint
import com.edugo.kmp.design.tokens.breakpointFromWidth
import com.edugo.kmp.dynamicui.model.NavigationItem
import com.edugo.kmp.dynamicui.model.firstLeaf

/**
 * Adaptive navigation layout with 3 breakpoints:
 * - EXPANDED (>= 840dp): PermanentNavigationDrawer with expandable sections
 * - MEDIUM (600-840dp): NavigationRail with top-level items + secondary tabs for children
 * - COMPACT (< 600dp): BottomNavigationBar with top-level items + secondary tabs for children
 */
@Composable
fun AdaptiveNavigationLayout(
    items: List<NavigationItem>,
    selectedKey: String?,
    expandedKeys: Set<String>,
    onItemSelected: (NavigationItem) -> Unit,
    onExpandToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
    header: @Composable ((compact: Boolean) -> Unit)? = null,
    content: @Composable (Modifier) -> Unit,
) {
    BoxWithConstraints(modifier = modifier) {
        val breakpoint = breakpointFromWidth(maxWidth.value.toInt())

        // Find the parent that owns the currently selected child (for secondary tabs)
        val selectedParent = items.find { item ->
            item.children.any { it.key == selectedKey }
        }

        when (breakpoint) {
            Breakpoint.EXPANDED -> {
                // Full sidebar with expandable sections
                val sections = items.map { it.toDrawerSection() }

                PermanentNavigationDrawer(
                    drawerContent = {
                        DSExpandableDrawerContent(
                            sections = sections,
                            selectedKey = selectedKey,
                            expandedKeys = expandedKeys,
                            onSectionClick = { section ->
                                val navItem = items.find { it.key == section.key }
                                if (navItem != null) onItemSelected(navItem)
                            },
                            onChildClick = { child ->
                                val navItem = items.flatMap { it.children }.find { it.key == child.key }
                                if (navItem != null) onItemSelected(navItem)
                            },
                            onExpandToggle = onExpandToggle,
                            header = if (header != null) {
                                { header(false) }
                            } else null,
                        )
                    },
                ) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        content(Modifier.fillMaxSize())
                    }
                }
            }

            Breakpoint.MEDIUM -> {
                // NavigationRail with top-level items + secondary tabs for children
                val railItems = items.map { item ->
                    DSNavigationRailItem(
                        label = item.label,
                        icon = resolveIcon(item.icon, filled = false),
                        selectedIcon = resolveIcon(item.icon, filled = true),
                    )
                }

                val selectedIndex = items.indexOfFirst { item ->
                    item.key == selectedKey || item.children.any { it.key == selectedKey }
                }.coerceAtLeast(0)

                Row(modifier = Modifier.fillMaxSize()) {
                    DSNavigationRail(
                        items = railItems,
                        selectedIndex = selectedIndex,
                        onItemSelected = { index ->
                            val item = items.getOrNull(index) ?: return@DSNavigationRail
                            val target = if (item.children.isNotEmpty()) {
                                item.children.firstLeaf() ?: item
                            } else {
                                item
                            }
                            onItemSelected(target)
                        },
                        header = if (header != null) {
                            { header(true) }
                        } else null,
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        // Secondary tabs for children of selected parent
                        if (selectedParent != null && selectedParent.children.size > 1) {
                            ChildrenTabRow(
                                parent = selectedParent,
                                selectedKey = selectedKey,
                                onChildSelected = onItemSelected,
                            )
                        }
                        Surface(modifier = Modifier.weight(1f)) {
                            content(Modifier.fillMaxSize())
                        }
                    }
                }
            }

            Breakpoint.COMPACT -> {
                // Bottom navigation with top-level items (max 5) + secondary tabs
                val topItems = items.take(5)
                val navBarItems = topItems.map { item ->
                    DSNavigationBarItem(
                        label = item.label,
                        icon = resolveIcon(item.icon, filled = false),
                        selectedIcon = resolveIcon(item.icon, filled = true),
                    )
                }

                val selectedIndex = topItems.indexOfFirst { item ->
                    item.key == selectedKey || item.children.any { it.key == selectedKey }
                }.coerceAtLeast(0)

                Scaffold(
                    topBar = {
                        Column {
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
                                        Column { header(true) }
                                    }
                                }
                            }
                            // Secondary tabs for children of selected parent
                            if (selectedParent != null && selectedParent.children.size > 1) {
                                ChildrenTabRow(
                                    parent = selectedParent,
                                    selectedKey = selectedKey,
                                    onChildSelected = onItemSelected,
                                )
                            }
                        }
                    },
                    bottomBar = {
                        if (navBarItems.isNotEmpty()) {
                            DSBottomNavigationBar(
                                items = navBarItems,
                                selectedIndex = selectedIndex,
                                onItemSelected = { index ->
                                    val item = topItems.getOrNull(index) ?: return@DSBottomNavigationBar
                                    val target = if (item.children.isNotEmpty()) {
                                        item.children.firstLeaf() ?: item
                                    } else {
                                        item
                                    }
                                    onItemSelected(target)
                                },
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
 * Secondary tab row showing children of a selected parent navigation item.
 * Used in MEDIUM and COMPACT breakpoints where the sidebar is not available.
 */
@Composable
private fun ChildrenTabRow(
    parent: NavigationItem,
    selectedKey: String?,
    onChildSelected: (NavigationItem) -> Unit,
) {
    val selectedChildIndex = parent.children
        .indexOfFirst { it.key == selectedKey }
        .coerceAtLeast(0)

    ScrollableTabRow(
        selectedTabIndex = selectedChildIndex,
        edgePadding = 16.dp,
    ) {
        parent.children.forEach { child ->
            Tab(
                selected = child.key == selectedKey,
                onClick = { onChildSelected(child) },
                text = { Text(child.label) },
                icon = child.icon?.let { iconName ->
                    {
                        Icon(
                            imageVector = resolveIcon(iconName, filled = child.key == selectedKey),
                            contentDescription = child.label,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                },
            )
        }
    }
}

/** Convert a NavigationItem to a DSDrawerSection for the expandable drawer. */
private fun NavigationItem.toDrawerSection(): DSDrawerSection = DSDrawerSection(
    key = key,
    label = label,
    icon = resolveIcon(icon, filled = false),
    selectedIcon = resolveIcon(icon, filled = true),
    children = children.map { child ->
        DSDrawerChild(
            key = child.key,
            label = child.label,
            icon = child.icon?.let { resolveIcon(it, filled = false) },
            selectedIcon = child.icon?.let { resolveIcon(it, filled = true) },
        )
    },
)

/**
 * Maps icon name strings from backend to Material Icons.
 */
internal fun resolveIcon(iconName: String?, filled: Boolean): ImageVector {
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

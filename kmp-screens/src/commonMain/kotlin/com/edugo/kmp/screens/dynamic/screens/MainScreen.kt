package com.edugo.kmp.screens.dynamic.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.edugo.kmp.design.components.navigation.DSBottomNavigationBar
import com.edugo.kmp.design.components.navigation.DSNavigationBarItem

/**
 * Pantalla principal con navegacion inferior (3 tabs).
 *
 * Tabs:
 * - Dashboard: Vista general con KPIs y actividad
 * - Materials: Lista de materiales educativos
 * - Settings: Configuracion de la app
 *
 * Cada tab renderiza su DynamicScreen correspondiente.
 */
@Composable
fun MainScreen(
    onNavigate: (String, Map<String, String>) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val navItems = remember {
        listOf(
            DSNavigationBarItem(
                label = "Dashboard",
                icon = Icons.Outlined.Dashboard,
                selectedIcon = Icons.Filled.Dashboard,
            ),
            DSNavigationBarItem(
                label = "Materials",
                icon = Icons.Outlined.FolderOpen,
                selectedIcon = Icons.Filled.Folder,
            ),
            DSNavigationBarItem(
                label = "Settings",
                icon = Icons.Outlined.Settings,
                selectedIcon = Icons.Filled.Settings,
            ),
        )
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            DSBottomNavigationBar(
                items = navItems,
                selectedIndex = selectedTab,
                onItemSelected = { selectedTab = it },
            )
        },
    ) { paddingValues ->
        when (selectedTab) {
            0 -> DynamicDashboardScreen(
                onNavigate = onNavigate,
                modifier = Modifier.padding(paddingValues),
            )
            1 -> DynamicMaterialsListScreen(
                onNavigate = onNavigate,
                modifier = Modifier.padding(paddingValues),
            )
            2 -> DynamicSettingsScreen(
                onBack = { selectedTab = 0 },
                onLogout = onLogout,
                onNavigate = onNavigate,
                modifier = Modifier.padding(paddingValues),
            )
        }
    }
}

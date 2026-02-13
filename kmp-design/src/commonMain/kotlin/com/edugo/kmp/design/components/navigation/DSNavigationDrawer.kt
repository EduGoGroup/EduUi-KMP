package com.edugo.kmp.design.components.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.edugo.kmp.design.Spacing

data class DSDrawerItem(
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector? = null,
)

@Composable
fun DSModalNavigationDrawer(
    items: List<DSDrawerItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed),
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        modifier = modifier,
        drawerContent = {
            ModalDrawerSheet {
                items.forEachIndexed { index, item ->
                    NavigationDrawerItem(
                        label = { Text(item.label) },
                        selected = selectedIndex == index,
                        onClick = { onItemSelected(index) },
                        icon = {
                            Icon(
                                imageVector = if (selectedIndex == index) (item.selectedIcon ?: item.icon) else item.icon,
                                contentDescription = item.label,
                            )
                        },
                        modifier = Modifier.padding(horizontal = Spacing.spacing3),
                    )
                }
            }
        },
        content = content,
    )
}

@Composable
fun DSPermanentNavigationDrawer(
    items: List<DSDrawerItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    PermanentNavigationDrawer(
        modifier = modifier,
        drawerContent = {
            PermanentDrawerSheet {
                items.forEachIndexed { index, item ->
                    NavigationDrawerItem(
                        label = { Text(item.label) },
                        selected = selectedIndex == index,
                        onClick = { onItemSelected(index) },
                        icon = {
                            Icon(
                                imageVector = if (selectedIndex == index) (item.selectedIcon ?: item.icon) else item.icon,
                                contentDescription = item.label,
                            )
                        },
                        modifier = Modifier.padding(horizontal = Spacing.spacing3),
                    )
                }
            }
        },
        content = content,
    )
}

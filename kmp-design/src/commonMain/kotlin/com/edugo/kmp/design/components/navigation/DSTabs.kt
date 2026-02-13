package com.edugo.kmp.design.components.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

data class DSTabItem(
    val label: String,
    val icon: ImageVector? = null,
)

enum class DSTabVariant { PRIMARY, SECONDARY }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DSTabs(
    tabs: List<DSTabItem>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    variant: DSTabVariant = DSTabVariant.PRIMARY,
) {
    val tabContent: @Composable () -> Unit = {
        tabs.forEachIndexed { index, tab ->
            Tab(
                selected = selectedIndex == index,
                onClick = { onTabSelected(index) },
                text = { Text(tab.label) },
                icon = tab.icon?.let { icon -> { Icon(icon, contentDescription = tab.label) } },
            )
        }
    }

    when (variant) {
        DSTabVariant.PRIMARY -> PrimaryTabRow(
            selectedTabIndex = selectedIndex,
            modifier = modifier,
            tabs = tabContent,
        )
        DSTabVariant.SECONDARY -> SecondaryTabRow(
            selectedTabIndex = selectedIndex,
            modifier = modifier,
            tabs = tabContent,
        )
    }
}

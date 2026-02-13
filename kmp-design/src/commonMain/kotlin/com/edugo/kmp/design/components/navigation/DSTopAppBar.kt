package com.edugo.kmp.design.components.navigation

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

enum class DSTopAppBarVariant { SMALL, CENTER_ALIGNED, MEDIUM, LARGE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DSTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    variant: DSTopAppBarVariant = DSTopAppBarVariant.SMALL,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    when (variant) {
        DSTopAppBarVariant.SMALL -> TopAppBar(
            title = { Text(title) },
            modifier = modifier,
            navigationIcon = navigationIcon,
            actions = actions,
            scrollBehavior = scrollBehavior,
        )
        DSTopAppBarVariant.CENTER_ALIGNED -> CenterAlignedTopAppBar(
            title = { Text(title) },
            modifier = modifier,
            navigationIcon = navigationIcon,
            actions = actions,
            scrollBehavior = scrollBehavior,
        )
        DSTopAppBarVariant.MEDIUM -> MediumTopAppBar(
            title = { Text(title) },
            modifier = modifier,
            navigationIcon = navigationIcon,
            actions = actions,
            scrollBehavior = scrollBehavior,
        )
        DSTopAppBarVariant.LARGE -> LargeTopAppBar(
            title = { Text(title) },
            modifier = modifier,
            navigationIcon = navigationIcon,
            actions = actions,
            scrollBehavior = scrollBehavior,
        )
    }
}

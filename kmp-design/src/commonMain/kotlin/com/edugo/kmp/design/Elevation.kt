package com.edugo.kmp.design

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Valores de elevación para componentes.
 * Basados en Material Design 3 elevation levels.
 */
object Elevation {
    val level0: Dp = 0.dp
    val level1: Dp = 1.dp
    val level2: Dp = 3.dp
    val level3: Dp = 6.dp
    val level4: Dp = 8.dp
    val level5: Dp = 12.dp

    // Semantic aliases
    val card: Dp = level1
    val cardHover: Dp = level2
    val floatingButton: Dp = level2
    val modal: Dp = level3
    val drawer: Dp = level4

    // Component mappings per MD3
    val fab: Dp = level3
    val fabPressed: Dp = level1
    val fabLowered: Dp = level1
    val dialog: Dp = level3
    val navigationDrawer: Dp = level1
    val modalBottomSheet: Dp = level1
    val topAppBar: Dp = level0
    val topAppBarScrolled: Dp = level2
    val bottomAppBar: Dp = level2
    val navigationBar: Dp = level2
    val navigationRail: Dp = level0
    val menu: Dp = level2
    val snackbar: Dp = level3
}

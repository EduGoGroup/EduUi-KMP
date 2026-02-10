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

    val card: Dp = level1
    val cardHover: Dp = level2
    val floatingButton: Dp = level2
    val modal: Dp = level3
    val drawer: Dp = level4
}

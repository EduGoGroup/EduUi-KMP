package com.edugo.kmp.design.tokens

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Responsive breakpoints for adapting layouts across device sizes.
 *
 * Based on Material Design 3 window size classes:
 * - [COMPACT]: phones in portrait (~0-599dp)
 * - [MEDIUM]: tablets, phones in landscape (~600-839dp)
 * - [EXPANDED]: desktop, large tablets (~840dp+)
 */
enum class Breakpoint(val minWidthDp: Int) {
    COMPACT(0),
    MEDIUM(600),
    EXPANDED(840),
}

/**
 * Resolves the [Breakpoint] for the given screen [widthDp].
 */
fun breakpointFromWidth(widthDp: Int): Breakpoint = when {
    widthDp >= Breakpoint.EXPANDED.minWidthDp -> Breakpoint.EXPANDED
    widthDp >= Breakpoint.MEDIUM.minWidthDp -> Breakpoint.MEDIUM
    else -> Breakpoint.COMPACT
}

/**
 * Responsive spacing presets per breakpoint.
 */
object ResponsiveSpacing {
    /** Horizontal content padding per breakpoint. */
    fun contentPadding(breakpoint: Breakpoint): Dp = when (breakpoint) {
        Breakpoint.COMPACT -> 16.dp
        Breakpoint.MEDIUM -> 24.dp
        Breakpoint.EXPANDED -> 32.dp
    }

    /** Gap between grid items per breakpoint. */
    fun gridGap(breakpoint: Breakpoint): Dp = when (breakpoint) {
        Breakpoint.COMPACT -> 8.dp
        Breakpoint.MEDIUM -> 16.dp
        Breakpoint.EXPANDED -> 24.dp
    }

    /** Recommended grid column count per breakpoint. */
    fun gridColumns(breakpoint: Breakpoint): Int = when (breakpoint) {
        Breakpoint.COMPACT -> 1
        Breakpoint.MEDIUM -> 2
        Breakpoint.EXPANDED -> 3
    }
}

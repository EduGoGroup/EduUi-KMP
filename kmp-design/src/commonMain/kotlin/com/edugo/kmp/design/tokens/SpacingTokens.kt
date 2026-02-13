package com.edugo.kmp.design.tokens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.edugo.kmp.design.Spacing

/**
 * Component-specific spacing presets.
 */
object ButtonSpacing {
    val verticalPadding = 10.dp
    val horizontalPadding = 24.dp
    val iconSpacing = 8.dp
}

object CardSpacing {
    val internalPadding = 16.dp
    val betweenCards = 16.dp
}

object ListSpacing {
    val itemVerticalPadding = 8.dp
    val itemHorizontalPadding = 16.dp
    val betweenItems = 0.dp
    val sectionHeaderPadding = 16.dp
}

object FormSpacing {
    val betweenFields = 16.dp
    val labelToField = 4.dp
    val fieldToHelper = 4.dp
    val sectionGap = 24.dp
}

/**
 * Horizontal spacer using design system spacing tokens.
 */
@Composable
fun DSHorizontalSpacer(width: androidx.compose.ui.unit.Dp = Spacing.spacing4) {
    Spacer(modifier = Modifier.width(width))
}

/**
 * Vertical spacer using design system spacing tokens.
 */
@Composable
fun DSVerticalSpacer(height: androidx.compose.ui.unit.Dp = Spacing.spacing4) {
    Spacer(modifier = Modifier.height(height))
}

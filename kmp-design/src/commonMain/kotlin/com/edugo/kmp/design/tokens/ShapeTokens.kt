package com.edugo.kmp.design.tokens

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * Corner radius raw values following MD3 shape scale.
 */
object CornerRadius {
    val none = 0.dp
    val extraSmall = 4.dp
    val small = 8.dp
    val medium = 12.dp
    val large = 16.dp
    val extraLarge = 28.dp
    val full = 50.dp
}

/**
 * Component-specific corner radius mappings per MD3 spec.
 */
object ComponentShapes {
    val checkbox = CornerRadius.extraSmall       // 4.dp
    val chip = CornerRadius.small                // 8.dp
    val card = CornerRadius.medium               // 12.dp
    val button = CornerRadius.large              // 16.dp (filled button with text)
    val fab = CornerRadius.large                 // 16.dp
    val bottomSheet = CornerRadius.extraLarge    // 28.dp (top corners only)
    val dialog = CornerRadius.extraLarge         // 28.dp
}

/**
 * Pre-built RoundedCornerShape instances.
 */
object Shapes {
    val none = RoundedCornerShape(CornerRadius.none)
    val extraSmall = RoundedCornerShape(CornerRadius.extraSmall)
    val small = RoundedCornerShape(CornerRadius.small)
    val medium = RoundedCornerShape(CornerRadius.medium)
    val large = RoundedCornerShape(CornerRadius.large)
    val extraLarge = RoundedCornerShape(CornerRadius.extraLarge)
    val full = RoundedCornerShape(CornerRadius.full)

    // Top-only shapes for bottom sheets
    val extraLargeTop = RoundedCornerShape(
        topStart = CornerRadius.extraLarge,
        topEnd = CornerRadius.extraLarge,
        bottomStart = CornerRadius.none,
        bottomEnd = CornerRadius.none
    )
}

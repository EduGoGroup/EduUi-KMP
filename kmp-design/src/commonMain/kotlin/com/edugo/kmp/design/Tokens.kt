package com.edugo.kmp.design

import androidx.compose.ui.unit.dp

/**
 * Espaciado estandar para padding, margins y gaps.
 */
object Spacing {
    val xxs = 4.dp
    val xs = 8.dp
    val s = 12.dp
    val m = 16.dp
    val l = 24.dp
    val xl = 32.dp
    val xxl = 48.dp
}

/**
 * Tamanos de componentes especificos.
 */
object Sizes {
    val iconSmall = 16.dp
    val iconMedium = 20.dp
    val iconLarge = 24.dp
    val iconXLarge = 32.dp
    val iconXXLarge = 48.dp
    val iconMassive = 64.dp

    val progressSmall = 24.dp
    val progressLarge = 48.dp

    val buttonHeight = 48.dp

    object Avatar {
        val small = 24.dp
        val medium = 32.dp
        val large = 40.dp
        val xlarge = 48.dp
        val xxlarge = 64.dp
    }

    object TouchTarget {
        val minimum = 48.dp
        val comfortable = 56.dp
    }
}

/**
 * Valores de opacidad/alpha para estados visuales.
 */
object Alpha {
    const val disabled = 0.4f
    const val muted = 0.6f
    const val subtle = 0.7f
    const val surfaceVariant = 0.8f
}

/**
 * Duraciones de animaciones y delays (en milisegundos).
 */
object Durations {
    const val splash = 2000L
    const val short = 200L
    const val medium = 500L
    const val long = 1000L
}

/**
 * Radios de esquinas (border radius).
 */
object Radius {
    val small = 4.dp
    val medium = 8.dp
    val large = 16.dp
}

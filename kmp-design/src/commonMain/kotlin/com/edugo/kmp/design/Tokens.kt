package com.edugo.kmp.design

import androidx.compose.ui.unit.dp
import com.edugo.kmp.design.tokens.SurfaceOpacity
import com.edugo.kmp.design.tokens.AnimationDuration
import com.edugo.kmp.design.tokens.ScreenDuration

/**
 * Spacing scale from 0dp to 64dp (spacing0 to spacing16).
 * Legacy aliases (xxs, xs, etc.) are deprecated with ReplaceWith.
 */
object Spacing {
    val spacing0 = 0.dp
    val spacing1 = 4.dp
    val spacing2 = 8.dp
    val spacing3 = 12.dp
    val spacing4 = 16.dp
    val spacing5 = 20.dp
    val spacing6 = 24.dp
    val spacing7 = 28.dp
    val spacing8 = 32.dp
    val spacing9 = 36.dp
    val spacing10 = 40.dp
    val spacing11 = 44.dp
    val spacing12 = 48.dp
    val spacing13 = 52.dp
    val spacing14 = 56.dp
    val spacing15 = 60.dp
    val spacing16 = 64.dp

    // Deprecated aliases
    @Deprecated("Use spacing1 instead", ReplaceWith("spacing1"))
    val xxs = spacing1

    @Deprecated("Use spacing2 instead", ReplaceWith("spacing2"))
    val xs = spacing2

    @Deprecated("Use spacing3 instead", ReplaceWith("spacing3"))
    val s = spacing3

    @Deprecated("Use spacing4 instead", ReplaceWith("spacing4"))
    val m = spacing4

    @Deprecated("Use spacing6 instead", ReplaceWith("spacing6"))
    val l = spacing6

    @Deprecated("Use spacing8 instead", ReplaceWith("spacing8"))
    val xl = spacing8

    @Deprecated("Use spacing12 instead", ReplaceWith("spacing12"))
    val xxl = spacing12
}

/**
 * Tamaños de componentes específicos.
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
        val generous = 64.dp
    }
}

/**
 * Valores de opacidad/alpha para estados visuales.
 * @see com.edugo.kmp.design.tokens.StateLayer para valores MD3 de state layers.
 * @see com.edugo.kmp.design.tokens.SurfaceOpacity para opacidades de superficie.
 */
object Alpha {
    @Deprecated("Use StateLayer.disabled instead", ReplaceWith("com.edugo.kmp.design.tokens.StateLayer.disabled"))
    const val disabled = 0.4f

    @Deprecated("Use SurfaceOpacity.high instead", ReplaceWith("com.edugo.kmp.design.tokens.SurfaceOpacity.high"))
    const val muted = 0.6f

    @Deprecated("Use SurfaceOpacity.high instead", ReplaceWith("com.edugo.kmp.design.tokens.SurfaceOpacity.high"))
    const val subtle = 0.7f

    @Deprecated("Use SurfaceOpacity.opaque instead", ReplaceWith("com.edugo.kmp.design.tokens.SurfaceOpacity.opaque"))
    const val surfaceVariant = 0.8f
}

/**
 * Duraciones de animaciones y delays (en milisegundos).
 * @see com.edugo.kmp.design.tokens.AnimationDuration para duraciones granulares MD3.
 * @see com.edugo.kmp.design.tokens.ScreenDuration para duraciones de pantalla.
 */
object Durations {
    @Deprecated("Use ScreenDuration.splash instead", ReplaceWith("com.edugo.kmp.design.tokens.ScreenDuration.splash"))
    const val splash = 2000L

    @Deprecated("Use AnimationDuration.short2 instead", ReplaceWith("com.edugo.kmp.design.tokens.AnimationDuration.short2"))
    const val short = 200L

    @Deprecated("Use AnimationDuration.long2 instead", ReplaceWith("com.edugo.kmp.design.tokens.AnimationDuration.long2"))
    const val medium = 500L

    @Deprecated("Use AnimationDuration.extraLong2 instead", ReplaceWith("com.edugo.kmp.design.tokens.AnimationDuration.extraLong2"))
    const val long = 1000L
}

/**
 * Radios de esquinas (border radius).
 * @see com.edugo.kmp.design.tokens.CornerRadius para valores MD3 completos.
 * @see com.edugo.kmp.design.tokens.Shapes para RoundedCornerShape pre-construidos.
 */
object Radius {
    @Deprecated("Use CornerRadius.extraSmall instead", ReplaceWith("com.edugo.kmp.design.tokens.CornerRadius.extraSmall"))
    val small = 4.dp

    @Deprecated("Use CornerRadius.small instead", ReplaceWith("com.edugo.kmp.design.tokens.CornerRadius.small"))
    val medium = 8.dp

    @Deprecated("Use CornerRadius.large instead", ReplaceWith("com.edugo.kmp.design.tokens.CornerRadius.large"))
    val large = 16.dp
}

package com.edugo.kmp.design.tokens

/**
 * State layer opacity values per MD3 interaction states.
 */
object StateLayer {
    const val hover = 0.08f
    const val focus = 0.12f
    const val pressed = 0.12f
    const val dragged = 0.16f
    const val disabled = 0.38f
    const val disabledContent = 0.38f
    const val disabledContainer = 0.12f
}

/**
 * Surface/element opacity values.
 */
object SurfaceOpacity {
    const val transparent = 0f
    const val faint = 0.05f
    const val light = 0.1f
    const val medium = 0.38f
    const val high = 0.6f
    const val opaque = 0.87f
    const val full = 1f
}

/**
 * Effect-specific opacity values.
 */
object EffectOpacity {
    const val shadowLight = 0.05f
    const val shadowMedium = 0.15f
    const val scrim = 0.4f
    const val scrimDark = 0.64f
}

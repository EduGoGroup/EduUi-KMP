package com.edugo.kmp.design.tokens

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.FastOutLinearInEasing

/**
 * Animation duration values in milliseconds.
 */
object AnimationDuration {
    const val extraShort1 = 50L
    const val extraShort2 = 100L
    const val short1 = 150L
    const val short2 = 200L
    const val medium1 = 250L
    const val medium2 = 300L
    const val long1 = 400L
    const val long2 = 500L
    const val extraLong1 = 700L
    const val extraLong2 = 1000L
}

/**
 * Screen-level and system durations in milliseconds.
 */
object ScreenDuration {
    const val splash = 2000L
    const val toastShort = 2000L
    const val toastLong = 3500L
    const val snackbar = 3000L
}

/**
 * Interactive element animation durations in milliseconds.
 */
object InteractiveDuration {
    const val rippleIn = 150L
    const val rippleOut = 100L
    const val buttonPress = 100L
    const val switchToggle = 200L
    const val checkboxCheck = 150L
}

/**
 * Stagger delay for sequential animations.
 */
object StaggerDelay {
    const val listItem = 50L
    const val gridItem = 30L
    const val chip = 40L
}

/**
 * Easing curves following MD3 motion spec.
 */
object AnimationEasing {
    val standard = FastOutSlowInEasing
    val standardDecelerate = LinearOutSlowInEasing
    val standardAccelerate = FastOutLinearInEasing
    val emphasized = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val emphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
    val emphasizedAccelerate = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)
}

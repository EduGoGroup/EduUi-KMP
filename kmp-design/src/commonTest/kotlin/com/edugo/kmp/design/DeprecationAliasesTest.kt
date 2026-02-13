@file:Suppress("DEPRECATION")

package com.edugo.kmp.design

import androidx.compose.ui.unit.dp
import com.edugo.kmp.design.tokens.AnimationDuration
import com.edugo.kmp.design.tokens.CornerRadius
import com.edugo.kmp.design.tokens.ScreenDuration
import com.edugo.kmp.design.tokens.StateLayer
import com.edugo.kmp.design.tokens.SurfaceOpacity
import kotlin.test.Test
import kotlin.test.assertEquals

class DeprecationAliasesTest {

    @Test
    fun spacingAliasesResolveToCorrectNewValues() {
        assertEquals(4.dp, Spacing.xxs)
        assertEquals(Spacing.spacing1, Spacing.xxs)

        assertEquals(8.dp, Spacing.xs)
        assertEquals(Spacing.spacing2, Spacing.xs)

        assertEquals(12.dp, Spacing.s)
        assertEquals(Spacing.spacing3, Spacing.s)

        assertEquals(16.dp, Spacing.m)
        assertEquals(Spacing.spacing4, Spacing.m)

        assertEquals(24.dp, Spacing.l)
        assertEquals(Spacing.spacing6, Spacing.l)

        assertEquals(32.dp, Spacing.xl)
        assertEquals(Spacing.spacing8, Spacing.xl)

        assertEquals(48.dp, Spacing.xxl)
        assertEquals(Spacing.spacing12, Spacing.xxl)
    }

    @Test
    fun alphaAliasesPreserveOriginalValues() {
        // Alpha values are preserved for backward compat.
        // The @Deprecated ReplaceWith points to semantically similar (not identical) new tokens.
        assertEquals(0.4f, Alpha.disabled)
        assertEquals(0.6f, Alpha.muted)
        assertEquals(0.7f, Alpha.subtle)
        assertEquals(0.8f, Alpha.surfaceVariant)
    }

    @Test
    fun durationAliasesHaveSameValues() {
        assertEquals(2000L, Durations.splash)
        assertEquals(ScreenDuration.splash, Durations.splash)

        assertEquals(200L, Durations.short)
        assertEquals(AnimationDuration.short2, Durations.short)

        assertEquals(1000L, Durations.long)
        assertEquals(AnimationDuration.extraLong2, Durations.long)
    }

    @Test
    fun radiusAliasesHaveSameValues() {
        assertEquals(4.dp, Radius.small)
        assertEquals(CornerRadius.extraSmall, Radius.small)

        assertEquals(8.dp, Radius.medium)
        assertEquals(CornerRadius.small, Radius.medium)

        assertEquals(16.dp, Radius.large)
        assertEquals(CornerRadius.large, Radius.large)
    }
}

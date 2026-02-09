package com.edugo.kmp.design

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DesignTokensTest {

    @Test
    fun spacingValuesAreOrdered() {
        assertTrue(Spacing.xxs < Spacing.xs)
        assertTrue(Spacing.xs < Spacing.s)
        assertTrue(Spacing.s < Spacing.m)
        assertTrue(Spacing.m < Spacing.l)
        assertTrue(Spacing.l < Spacing.xl)
        assertTrue(Spacing.xl < Spacing.xxl)
    }

    @Test
    fun spacingValuesMatchExpected() {
        assertEquals(4.dp, Spacing.xxs)
        assertEquals(8.dp, Spacing.xs)
        assertEquals(12.dp, Spacing.s)
        assertEquals(16.dp, Spacing.m)
        assertEquals(24.dp, Spacing.l)
        assertEquals(32.dp, Spacing.xl)
        assertEquals(48.dp, Spacing.xxl)
    }

    @Test
    fun sizesIconsAreOrdered() {
        assertTrue(Sizes.iconSmall < Sizes.iconMedium)
        assertTrue(Sizes.iconMedium < Sizes.iconLarge)
        assertTrue(Sizes.iconLarge < Sizes.iconXLarge)
        assertTrue(Sizes.iconXLarge < Sizes.iconXXLarge)
        assertTrue(Sizes.iconXXLarge < Sizes.iconMassive)
    }

    @Test
    fun sizesAvatarsAreOrdered() {
        assertTrue(Sizes.Avatar.small < Sizes.Avatar.medium)
        assertTrue(Sizes.Avatar.medium < Sizes.Avatar.large)
        assertTrue(Sizes.Avatar.large < Sizes.Avatar.xlarge)
        assertTrue(Sizes.Avatar.xlarge < Sizes.Avatar.xxlarge)
    }

    @Test
    fun sizesTouchTargetMinimumMeetsGuidelines() {
        assertTrue(Sizes.TouchTarget.minimum >= 48.dp)
    }

    @Test
    fun alphaValuesAreInRange() {
        assertTrue(Alpha.disabled in 0f..1f)
        assertTrue(Alpha.muted in 0f..1f)
        assertTrue(Alpha.subtle in 0f..1f)
        assertTrue(Alpha.surfaceVariant in 0f..1f)
    }

    @Test
    fun alphaValuesAreOrdered() {
        assertTrue(Alpha.disabled < Alpha.muted)
        assertTrue(Alpha.muted < Alpha.subtle)
        assertTrue(Alpha.subtle < Alpha.surfaceVariant)
    }

    @Test
    fun durationsArePositive() {
        assertTrue(Durations.splash > 0)
        assertTrue(Durations.short > 0)
        assertTrue(Durations.medium > 0)
        assertTrue(Durations.long > 0)
    }

    @Test
    fun durationsAreOrdered() {
        assertTrue(Durations.short < Durations.medium)
        assertTrue(Durations.medium < Durations.long)
        assertTrue(Durations.long < Durations.splash)
    }

    @Test
    fun radiusValuesAreOrdered() {
        assertTrue(Radius.small < Radius.medium)
        assertTrue(Radius.medium < Radius.large)
    }

    @Test
    fun elevationLevelsAreOrdered() {
        assertTrue(Elevation.level0 < Elevation.level1)
        assertTrue(Elevation.level1 < Elevation.level2)
        assertTrue(Elevation.level2 < Elevation.level3)
        assertTrue(Elevation.level3 < Elevation.level4)
        assertTrue(Elevation.level4 < Elevation.level5)
    }

    @Test
    fun elevationAliasesMatchLevels() {
        assertEquals(Elevation.level1, Elevation.card)
        assertEquals(Elevation.level2, Elevation.cardHover)
        assertEquals(Elevation.level2, Elevation.floatingButton)
        assertEquals(Elevation.level3, Elevation.modal)
        assertEquals(Elevation.level4, Elevation.drawer)
    }

    @Test
    fun messageTypeHasAllValues() {
        val types = MessageType.entries
        assertEquals(4, types.size)
        assertTrue(types.contains(MessageType.INFO))
        assertTrue(types.contains(MessageType.SUCCESS))
        assertTrue(types.contains(MessageType.WARNING))
        assertTrue(types.contains(MessageType.ERROR))
    }
}

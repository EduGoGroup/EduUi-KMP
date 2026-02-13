package com.edugo.kmp.design

import com.edugo.kmp.design.tokens.AnimationDuration
import com.edugo.kmp.design.tokens.InteractiveDuration
import com.edugo.kmp.design.tokens.ScreenDuration
import com.edugo.kmp.design.tokens.StaggerDelay
import kotlin.test.Test
import kotlin.test.assertTrue

class MotionTokensTest {

    @Test
    fun animationDurationScaleIsOrdered() {
        assertTrue(AnimationDuration.extraShort1 < AnimationDuration.extraShort2)
        assertTrue(AnimationDuration.extraShort2 < AnimationDuration.short1)
        assertTrue(AnimationDuration.short1 < AnimationDuration.short2)
        assertTrue(AnimationDuration.short2 < AnimationDuration.medium1)
        assertTrue(AnimationDuration.medium1 < AnimationDuration.medium2)
        assertTrue(AnimationDuration.medium2 < AnimationDuration.long1)
        assertTrue(AnimationDuration.long1 < AnimationDuration.long2)
        assertTrue(AnimationDuration.long2 < AnimationDuration.extraLong1)
        assertTrue(AnimationDuration.extraLong1 < AnimationDuration.extraLong2)
    }

    @Test
    fun animationDurationsArePositive() {
        assertTrue(AnimationDuration.extraShort1 > 0)
        assertTrue(AnimationDuration.extraLong2 > 0)
    }

    @Test
    fun screenDurationsArePositive() {
        assertTrue(ScreenDuration.splash > 0)
        assertTrue(ScreenDuration.toastShort > 0)
        assertTrue(ScreenDuration.toastLong > 0)
        assertTrue(ScreenDuration.snackbar > 0)
    }

    @Test
    fun screenDurationsToastOrdering() {
        assertTrue(ScreenDuration.toastShort < ScreenDuration.toastLong)
    }

    @Test
    fun interactiveDurationsArePositive() {
        assertTrue(InteractiveDuration.rippleIn > 0)
        assertTrue(InteractiveDuration.rippleOut > 0)
        assertTrue(InteractiveDuration.buttonPress > 0)
        assertTrue(InteractiveDuration.switchToggle > 0)
        assertTrue(InteractiveDuration.checkboxCheck > 0)
    }

    @Test
    fun staggerDelaysArePositive() {
        assertTrue(StaggerDelay.listItem > 0)
        assertTrue(StaggerDelay.gridItem > 0)
        assertTrue(StaggerDelay.chip > 0)
    }
}

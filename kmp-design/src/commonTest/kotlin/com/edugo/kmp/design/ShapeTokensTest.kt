package com.edugo.kmp.design

import androidx.compose.ui.unit.dp
import com.edugo.kmp.design.tokens.ComponentShapes
import com.edugo.kmp.design.tokens.CornerRadius
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ShapeTokensTest {

    @Test
    fun cornerRadiusScaleIsOrdered() {
        assertTrue(CornerRadius.none < CornerRadius.extraSmall)
        assertTrue(CornerRadius.extraSmall < CornerRadius.small)
        assertTrue(CornerRadius.small < CornerRadius.medium)
        assertTrue(CornerRadius.medium < CornerRadius.large)
        assertTrue(CornerRadius.large < CornerRadius.extraLarge)
        assertTrue(CornerRadius.extraLarge < CornerRadius.full)
    }

    @Test
    fun cornerRadiusValuesMatchSpec() {
        assertEquals(0.dp, CornerRadius.none)
        assertEquals(4.dp, CornerRadius.extraSmall)
        assertEquals(8.dp, CornerRadius.small)
        assertEquals(12.dp, CornerRadius.medium)
        assertEquals(16.dp, CornerRadius.large)
        assertEquals(28.dp, CornerRadius.extraLarge)
        assertEquals(50.dp, CornerRadius.full)
    }

    @Test
    fun componentShapesMapToCorrectRadius() {
        assertEquals(CornerRadius.extraSmall, ComponentShapes.checkbox)
        assertEquals(CornerRadius.small, ComponentShapes.chip)
        assertEquals(CornerRadius.medium, ComponentShapes.card)
        assertEquals(CornerRadius.large, ComponentShapes.button)
        assertEquals(CornerRadius.large, ComponentShapes.fab)
        assertEquals(CornerRadius.extraLarge, ComponentShapes.bottomSheet)
        assertEquals(CornerRadius.extraLarge, ComponentShapes.dialog)
    }
}

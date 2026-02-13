package com.edugo.kmp.design

import com.edugo.kmp.design.tokens.EffectOpacity
import com.edugo.kmp.design.tokens.StateLayer
import com.edugo.kmp.design.tokens.SurfaceOpacity
import kotlin.test.Test
import kotlin.test.assertTrue

class OpacityTokensTest {

    @Test
    fun stateLayerValuesAreInRange() {
        assertTrue(StateLayer.hover in 0f..1f)
        assertTrue(StateLayer.focus in 0f..1f)
        assertTrue(StateLayer.pressed in 0f..1f)
        assertTrue(StateLayer.dragged in 0f..1f)
        assertTrue(StateLayer.disabled in 0f..1f)
        assertTrue(StateLayer.disabledContent in 0f..1f)
        assertTrue(StateLayer.disabledContainer in 0f..1f)
    }

    @Test
    fun stateLayerValuesAreOrdered() {
        assertTrue(StateLayer.hover <= StateLayer.focus)
        assertTrue(StateLayer.focus <= StateLayer.pressed)
        assertTrue(StateLayer.pressed <= StateLayer.dragged)
        assertTrue(StateLayer.dragged <= StateLayer.disabled)
    }

    @Test
    fun surfaceOpacityValuesAreInRange() {
        assertTrue(SurfaceOpacity.transparent in 0f..1f)
        assertTrue(SurfaceOpacity.faint in 0f..1f)
        assertTrue(SurfaceOpacity.light in 0f..1f)
        assertTrue(SurfaceOpacity.medium in 0f..1f)
        assertTrue(SurfaceOpacity.high in 0f..1f)
        assertTrue(SurfaceOpacity.opaque in 0f..1f)
        assertTrue(SurfaceOpacity.full in 0f..1f)
    }

    @Test
    fun surfaceOpacityValuesAreOrdered() {
        assertTrue(SurfaceOpacity.transparent < SurfaceOpacity.faint)
        assertTrue(SurfaceOpacity.faint < SurfaceOpacity.light)
        assertTrue(SurfaceOpacity.light < SurfaceOpacity.medium)
        assertTrue(SurfaceOpacity.medium < SurfaceOpacity.high)
        assertTrue(SurfaceOpacity.high < SurfaceOpacity.opaque)
        assertTrue(SurfaceOpacity.opaque < SurfaceOpacity.full)
    }

    @Test
    fun effectOpacityValuesAreInRange() {
        assertTrue(EffectOpacity.shadowLight in 0f..1f)
        assertTrue(EffectOpacity.shadowMedium in 0f..1f)
        assertTrue(EffectOpacity.scrim in 0f..1f)
        assertTrue(EffectOpacity.scrimDark in 0f..1f)
    }

    @Test
    fun effectOpacityValuesAreOrdered() {
        assertTrue(EffectOpacity.shadowLight < EffectOpacity.shadowMedium)
        assertTrue(EffectOpacity.shadowMedium < EffectOpacity.scrim)
        assertTrue(EffectOpacity.scrim < EffectOpacity.scrimDark)
    }
}

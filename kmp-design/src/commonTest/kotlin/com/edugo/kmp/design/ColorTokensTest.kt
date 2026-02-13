package com.edugo.kmp.design

import com.edugo.kmp.design.tokens.ColorTokens
import com.edugo.kmp.design.tokens.ExtendedColorScheme
import kotlin.test.Test
import kotlin.test.assertNotEquals

class ColorTokensTest {

    @Test
    fun lightThemeHasAllPrimaryColors() {
        val l = ColorTokens.Light
        assertNotEquals(l.primary, l.onPrimary)
        assertNotEquals(l.primaryContainer, l.onPrimaryContainer)
    }

    @Test
    fun lightThemeHasAllSecondaryColors() {
        val l = ColorTokens.Light
        assertNotEquals(l.secondary, l.onSecondary)
        assertNotEquals(l.secondaryContainer, l.onSecondaryContainer)
    }

    @Test
    fun lightThemeHasAllTertiaryColors() {
        val l = ColorTokens.Light
        assertNotEquals(l.tertiary, l.onTertiary)
        assertNotEquals(l.tertiaryContainer, l.onTertiaryContainer)
    }

    @Test
    fun lightThemeHasAllErrorColors() {
        val l = ColorTokens.Light
        assertNotEquals(l.error, l.onError)
        assertNotEquals(l.errorContainer, l.onErrorContainer)
    }

    @Test
    fun lightThemeHasAllSurfaceColors() {
        val l = ColorTokens.Light
        assertNotEquals(l.surface, l.onSurface)
        assertNotEquals(l.surfaceVariant, l.onSurfaceVariant)
        assertNotEquals(l.surfaceContainerLowest, l.surfaceContainerHighest)
    }

    @Test
    fun lightThemeHasOutlineAndScrim() {
        val l = ColorTokens.Light
        assertNotEquals(l.outline, l.outlineVariant)
        assertNotEquals(l.scrim, l.surface)
    }

    @Test
    fun lightThemeHasInverseColors() {
        val l = ColorTokens.Light
        assertNotEquals(l.inverseSurface, l.inverseOnSurface)
    }

    @Test
    fun darkThemeHasAllPrimaryColors() {
        val d = ColorTokens.Dark
        assertNotEquals(d.primary, d.onPrimary)
        assertNotEquals(d.primaryContainer, d.onPrimaryContainer)
    }

    @Test
    fun darkThemeHasAllSecondaryColors() {
        val d = ColorTokens.Dark
        assertNotEquals(d.secondary, d.onSecondary)
        assertNotEquals(d.secondaryContainer, d.onSecondaryContainer)
    }

    @Test
    fun darkThemeHasAllTertiaryColors() {
        val d = ColorTokens.Dark
        assertNotEquals(d.tertiary, d.onTertiary)
        assertNotEquals(d.tertiaryContainer, d.onTertiaryContainer)
    }

    @Test
    fun darkThemeHasAllErrorColors() {
        val d = ColorTokens.Dark
        assertNotEquals(d.error, d.onError)
        assertNotEquals(d.errorContainer, d.onErrorContainer)
    }

    @Test
    fun darkThemeHasAllSurfaceColors() {
        val d = ColorTokens.Dark
        assertNotEquals(d.surface, d.onSurface)
        assertNotEquals(d.surfaceVariant, d.onSurfaceVariant)
        assertNotEquals(d.surfaceContainerLowest, d.surfaceContainerHighest)
    }

    @Test
    fun lightAndDarkPrimaryColorsDiffer() {
        assertNotEquals(ColorTokens.Light.primary, ColorTokens.Dark.primary)
    }

    @Test
    fun lightAndDarkSurfaceColorsDiffer() {
        assertNotEquals(ColorTokens.Light.surface, ColorTokens.Dark.surface)
    }

    @Test
    fun extendedLightSchemeHasDistinctColors() {
        val ext = ExtendedColorScheme.light()
        assertNotEquals(ext.success, ext.onSuccess)
        assertNotEquals(ext.warning, ext.onWarning)
        assertNotEquals(ext.info, ext.onInfo)
        assertNotEquals(ext.successContainer, ext.onSuccessContainer)
        assertNotEquals(ext.warningContainer, ext.onWarningContainer)
        assertNotEquals(ext.infoContainer, ext.onInfoContainer)
    }

    @Test
    fun extendedDarkSchemeHasDistinctColors() {
        val ext = ExtendedColorScheme.dark()
        assertNotEquals(ext.success, ext.onSuccess)
        assertNotEquals(ext.warning, ext.onWarning)
        assertNotEquals(ext.info, ext.onInfo)
        assertNotEquals(ext.successContainer, ext.onSuccessContainer)
        assertNotEquals(ext.warningContainer, ext.onWarningContainer)
        assertNotEquals(ext.infoContainer, ext.onInfoContainer)
    }

    @Test
    fun extendedLightAndDarkSchemesDiffer() {
        val light = ExtendedColorScheme.light()
        val dark = ExtendedColorScheme.dark()
        assertNotEquals(light.success, dark.success)
        assertNotEquals(light.warning, dark.warning)
        assertNotEquals(light.info, dark.info)
    }
}

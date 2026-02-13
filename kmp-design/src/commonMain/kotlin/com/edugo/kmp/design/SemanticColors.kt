package com.edugo.kmp.design

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.edugo.kmp.design.tokens.LocalExtendedColorScheme

/**
 * Colores semánticos para mensajes y estados.
 * Usa ExtendedColorScheme para success/warning/info y MaterialTheme para error.
 */
object SemanticColors {

    @Composable
    fun success(): Color = LocalExtendedColorScheme.current.success

    @Composable
    fun onSuccess(): Color = LocalExtendedColorScheme.current.onSuccess

    @Composable
    fun successContainer(): Color = LocalExtendedColorScheme.current.successContainer

    @Composable
    fun onSuccessContainer(): Color = LocalExtendedColorScheme.current.onSuccessContainer

    @Composable
    fun warning(): Color = LocalExtendedColorScheme.current.warning

    @Composable
    fun onWarning(): Color = LocalExtendedColorScheme.current.onWarning

    @Composable
    fun warningContainer(): Color = LocalExtendedColorScheme.current.warningContainer

    @Composable
    fun onWarningContainer(): Color = LocalExtendedColorScheme.current.onWarningContainer

    @Composable
    fun error(): Color = MaterialTheme.colorScheme.error

    @Composable
    fun onError(): Color = MaterialTheme.colorScheme.onError

    @Composable
    fun errorContainer(): Color = MaterialTheme.colorScheme.errorContainer

    @Composable
    fun onErrorContainer(): Color = MaterialTheme.colorScheme.onErrorContainer

    @Composable
    fun info(): Color = LocalExtendedColorScheme.current.info

    @Composable
    fun onInfo(): Color = LocalExtendedColorScheme.current.onInfo

    @Composable
    fun infoContainer(): Color = LocalExtendedColorScheme.current.infoContainer

    @Composable
    fun onInfoContainer(): Color = LocalExtendedColorScheme.current.onInfoContainer
}

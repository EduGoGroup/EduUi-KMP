package com.edugo.kmp.design

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Colores semánticos para mensajes y estados.
 * Usa MaterialTheme.colorScheme como base para mantener consistencia con el tema.
 */
object SemanticColors {

    @Composable
    fun success(): Color = MaterialTheme.colorScheme.tertiary

    @Composable
    fun onSuccess(): Color = MaterialTheme.colorScheme.onTertiary

    @Composable
    fun successContainer(): Color = MaterialTheme.colorScheme.tertiaryContainer

    @Composable
    fun onSuccessContainer(): Color = MaterialTheme.colorScheme.onTertiaryContainer

    @Composable
    fun warning(): Color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f)

    @Composable
    fun onWarning(): Color = MaterialTheme.colorScheme.onSecondary

    @Composable
    fun warningContainer(): Color = MaterialTheme.colorScheme.secondaryContainer

    @Composable
    fun onWarningContainer(): Color = MaterialTheme.colorScheme.onSecondaryContainer

    @Composable
    fun error(): Color = MaterialTheme.colorScheme.error

    @Composable
    fun onError(): Color = MaterialTheme.colorScheme.onError

    @Composable
    fun errorContainer(): Color = MaterialTheme.colorScheme.errorContainer

    @Composable
    fun onErrorContainer(): Color = MaterialTheme.colorScheme.onErrorContainer

    @Composable
    fun info(): Color = MaterialTheme.colorScheme.primary

    @Composable
    fun onInfo(): Color = MaterialTheme.colorScheme.onPrimary

    @Composable
    fun infoContainer(): Color = MaterialTheme.colorScheme.primaryContainer

    @Composable
    fun onInfoContainer(): Color = MaterialTheme.colorScheme.onPrimaryContainer
}

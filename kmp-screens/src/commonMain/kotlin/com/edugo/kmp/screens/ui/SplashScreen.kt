@file:Suppress("DEPRECATION")

package com.edugo.kmp.screens.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.edugo.kmp.auth.service.AuthService
import com.edugo.kmp.dynamicui.sync.DataSyncService
import com.edugo.kmp.design.EduGoTheme
import com.edugo.kmp.design.Sizes
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.tokens.ScreenDuration
import com.edugo.kmp.design.tokens.SurfaceOpacity
import com.edugo.kmp.resources.InitStringsForPreview
import com.edugo.kmp.resources.Strings
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

/**
 * Pantalla de splash - Primera pantalla al iniciar la aplicación.
 *
 * Muestra titulo y loading, luego auto-navega a Login o Home.
 */
@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
    delayMs: Long = ScreenDuration.splash,
) {
    val authService = koinInject<AuthService>()
    val dataSyncService = koinInject<DataSyncService>()

    LaunchedEffect(Unit) {
        // Phase 1: Restore auth + local bundle in parallel
        val authJob = async {
            try { authService.restoreSession() } catch (_: Exception) { }
        }
        val localJob = async { dataSyncService.restoreFromLocal() }

        authJob.await()
        localJob.await()

        if (authService.isAuthenticated()) {
            // Phase 2: Delta sync in parallel with splash delay
            val deltaJob = async {
                try { dataSyncService.deltaSync() } catch (_: Exception) { /* non-critical */ }
            }
            delay(delayMs)
            deltaJob.await()
            onNavigateToHome()
        } else {
            delay(delayMs)
            onNavigateToLogin()
        }
    }

    SplashScreenContent(modifier = modifier)
}

@Composable
private fun SplashScreenContent(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.spacing8),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = Strings.splash_title,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = Spacing.spacing4)
            )

            Text(
                text = Strings.splash_subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = SurfaceOpacity.high),
                modifier = Modifier.padding(bottom = Spacing.spacing12)
            )

            CircularProgressIndicator(
                modifier = Modifier.size(Sizes.progressLarge),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(Spacing.spacing4))

            Text(
                text = Strings.splash_loading,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = SurfaceOpacity.high)
            )
        }
    }
}

@Preview
@Composable
private fun SplashScreenPreview() {
    InitStringsForPreview()
    EduGoTheme {
        SplashScreenContent()
    }
}

@Preview
@Composable
private fun SplashScreenDarkPreview() {
    InitStringsForPreview()
    EduGoTheme(darkTheme = true) {
        SplashScreenContent()
    }
}

package com.edugo.web

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.edugo.kmp.config.Environment
import com.edugo.kmp.config.EnvironmentDetector
import com.edugo.kmp.dynamicui.platform.PlatformDetector
import com.edugo.kmp.dynamicui.platform.PlatformType
import com.edugo.kmp.screens.App
import kotlinx.browser.document

/**
 * Punto de entrada de la aplicación Web (Wasm).
 *
 * Environment is set at build time via Gradle property:
 *   ./gradlew wasmJsBrowserDevelopmentRun -Penv=STAGING
 */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    PlatformDetector.current = PlatformType.WEB

    if (BUILD_ENVIRONMENT.isNotEmpty()) {
        Environment.fromString(BUILD_ENVIRONMENT)?.let {
            EnvironmentDetector.forceEnvironment(it)
        }
    }

    ComposeViewport(document.body!!) {
        App()
    }
}

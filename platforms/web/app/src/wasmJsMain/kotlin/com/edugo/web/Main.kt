package com.edugo.web

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.edugo.kmp.dynamicui.platform.PlatformDetector
import com.edugo.kmp.dynamicui.platform.PlatformType
import com.edugo.kmp.screens.App
import kotlinx.browser.document

/**
 * Punto de entrada de la aplicación Web (Wasm).
 */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    PlatformDetector.current = PlatformType.WEB
    ComposeViewport(document.body!!) {
        App()
    }
}

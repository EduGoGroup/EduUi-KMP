package com.edugo.web

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.edugo.kmp.screens.App
import kotlinx.browser.document

/**
 * Punto de entrada de la aplicacion Web (Wasm).
 */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.body!!) {
        App()
    }
}

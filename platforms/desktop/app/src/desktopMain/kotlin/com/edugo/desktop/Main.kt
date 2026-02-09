package com.edugo.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.edugo.kmp.resources.Strings
import com.edugo.kmp.screens.App

/**
 * Punto de entrada de la aplicacion Desktop.
 */
fun main() {
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = Strings.app_name
        ) {
            App()
        }
    }
}

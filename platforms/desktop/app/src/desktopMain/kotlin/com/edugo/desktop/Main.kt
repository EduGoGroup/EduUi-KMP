package com.edugo.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.edugo.kmp.dynamicui.platform.PlatformDetector
import com.edugo.kmp.dynamicui.platform.PlatformType
import com.edugo.kmp.resources.Strings
import com.edugo.kmp.screens.App

/**
 * Punto de entrada de la aplicación Desktop.
 */
fun main() {
    val appName = "EduGo Desktop"
    println("Iniciando $appName...")
    PlatformDetector.current = PlatformType.DESKTOP
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = Strings.app_name
        ) {
            App()
        }
    }
}

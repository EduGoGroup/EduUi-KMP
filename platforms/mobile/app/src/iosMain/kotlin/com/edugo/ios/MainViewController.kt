package com.edugo.ios

import androidx.compose.ui.window.ComposeUIViewController
import com.edugo.kmp.dynamicui.platform.PlatformDetector
import com.edugo.kmp.dynamicui.platform.PlatformType
import com.edugo.kmp.screens.App

/**
 * Punto de entrada iOS.
 * Crea un UIViewController con la UI de Compose Multiplatform.
 * Es invocado desde Swift (AppDelegate) como:
 *   MainViewControllerKt.MainViewController()
 */
fun MainViewController() = ComposeUIViewController {
    PlatformDetector.current = PlatformType.IOS
    App()
}

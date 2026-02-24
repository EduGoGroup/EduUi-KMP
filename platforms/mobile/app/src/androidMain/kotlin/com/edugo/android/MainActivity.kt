package com.edugo.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.edugo.kmp.config.Environment
import com.edugo.kmp.config.EnvironmentDetector
import com.edugo.kmp.dynamicui.platform.PlatformDetector
import com.edugo.kmp.dynamicui.platform.PlatformType
import com.edugo.kmp.screens.App

/**
 * MainActivity - Punto de entrada de la aplicación Android.
 *
 * Environment is set at build time via Gradle property:
 *   ./gradlew installDebug -Penv=STAGING
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PlatformDetector.current = PlatformType.ANDROID

        if (BuildConfig.BUILD_ENVIRONMENT.isNotEmpty()) {
            Environment.fromString(BuildConfig.BUILD_ENVIRONMENT)?.let {
                EnvironmentDetector.forceEnvironment(it)
            }
        }

        setContent {
            App()
        }
    }
}

package com.edugo.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.edugo.kmp.screens.App

/**
 * MainActivity - Punto de entrada de la aplicacion Android.
 *
 * Usa el componente App compartido que gestiona:
 * - Koin DI
 * - EduGoTheme
 * - Navegacion: Splash -> Login -> Home -> Settings
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            App()
        }
    }
}

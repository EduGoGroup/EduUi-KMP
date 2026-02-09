package com.edugo.android

import android.app.Application
import com.edugo.kmp.resources.Strings

/**
 * Application class para Android.
 *
 * Inicializa:
 * - Strings con contexto Android para acceso a recursos nativos
 */
class EduGoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Strings.init(this)
    }
}

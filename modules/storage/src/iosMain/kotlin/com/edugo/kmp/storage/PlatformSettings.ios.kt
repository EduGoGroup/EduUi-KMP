package com.edugo.kmp.storage

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import platform.Foundation.NSUserDefaults

/**
 * Implementacion iOS de Settings usando NSUserDefaults.
 * Los datos se almacenan en el sandbox de la aplicacion.
 */
actual fun createPlatformSettings(): Settings {
    return NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults)
}

actual fun createPlatformSettings(name: String): Settings {
    return NSUserDefaultsSettings(NSUserDefaults(suiteName = "com.edugo.storage.$name")!!)
}

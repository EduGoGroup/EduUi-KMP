/*
 * Copyright (c) 2026 EduGo Project
 * Licensed under the MIT License
 */

package com.edugo.kmp.storage

import com.russhwolf.settings.Settings

actual fun createPlatformSettings(): Settings {
    return Settings()
}

actual fun createPlatformSettings(name: String): Settings {
    return Settings()
}

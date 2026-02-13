package com.edugo.kmp.di.module

import com.edugo.kmp.settings.theme.ThemeService
import com.edugo.kmp.settings.theme.ThemeServiceImpl
import com.edugo.kmp.storage.SafeEduGoStorage
import org.koin.dsl.module

/**
 * Modulo Koin para dependencias del modulo settings.
 *
 * Provee ThemeService como singleton.
 */
public val settingsModule = module {
    single<ThemeService> { ThemeServiceImpl(get<SafeEduGoStorage>()) }
}

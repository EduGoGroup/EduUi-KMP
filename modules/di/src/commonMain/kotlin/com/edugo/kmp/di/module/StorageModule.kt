package com.edugo.kmp.di.module

import com.edugo.kmp.storage.AsyncEduGoStorage
import com.edugo.kmp.storage.EduGoStorage
import com.edugo.kmp.storage.SafeEduGoStorage
import org.koin.dsl.module

/**
 * Modulo Koin para dependencias del modulo storage.
 *
 * Provee las 3 capas de storage como singletons.
 */
public val storageModule = module {
    single { EduGoStorage.create() }
    single { SafeEduGoStorage.wrap(get<EduGoStorage>()) }
    single { AsyncEduGoStorage(get<EduGoStorage>()) }
}

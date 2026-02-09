package com.edugo.kmp.di.module

import com.edugo.kmp.logger.DefaultLogger
import com.edugo.kmp.logger.Logger
import com.edugo.kmp.logger.TaggedLogger
import com.edugo.kmp.logger.withTag
import org.koin.dsl.module

/**
 * Modulo Koin para dependencias del modulo logger.
 *
 * Provee el Logger singleton y un factory para TaggedLogger.
 */
public val loggerModule = module {
    single<Logger> { DefaultLogger }
    factory { (tag: String) -> get<Logger>().withTag(tag) as TaggedLogger }
}

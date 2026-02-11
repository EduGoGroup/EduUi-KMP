package com.edugo.kmp.logger

import co.touchlab.kermit.Logger as KermitLoggerImpl

/**
 * Creates the default iOS Logger implementation.
 *
 * Delegates to Kermit which uses NSLogWriter automatically on iOS,
 * routing logs through NSLog visible in Xcode console.
 *
 * @return A [KermitDelegateLogger] backed by Kermit
 */
public actual fun createDefaultLogger(): Logger =
    KermitDelegateLogger(KermitLoggerImpl.withTag("EduGo"))

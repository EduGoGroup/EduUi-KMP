package com.edugo.kmp.logger

import co.touchlab.kermit.Logger as KermitLoggerImpl

/**
 * Creates the default JVM/Desktop Logger implementation.
 *
 * Delegates to Kermit which uses platformLogWriter on JVM,
 * providing ANSI-colored console output.
 *
 * @return A [KermitDelegateLogger] backed by Kermit
 */
public actual fun createDefaultLogger(): Logger =
    KermitDelegateLogger(KermitLoggerImpl.withTag("EduGo"))

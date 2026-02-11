package com.edugo.kmp.logger

import co.touchlab.kermit.Logger as KermitLoggerImpl

/**
 * Creates the default Android Logger implementation.
 *
 * Delegates to Kermit which uses LogcatWriter automatically on Android,
 * routing logs through android.util.Log.
 *
 * @return A [KermitDelegateLogger] backed by Kermit
 */
public actual fun createDefaultLogger(): Logger =
    KermitDelegateLogger(KermitLoggerImpl.withTag("EduGo"))

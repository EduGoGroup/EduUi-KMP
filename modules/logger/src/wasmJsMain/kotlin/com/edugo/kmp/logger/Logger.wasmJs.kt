package com.edugo.kmp.logger

import co.touchlab.kermit.Logger as KermitLoggerImpl

/**
 * Creates the default WasmJs Logger implementation.
 *
 * Delegates to Kermit which uses CommonWriter for wasmJs,
 * compatible with browser and Node.js environments.
 *
 * @return A [KermitDelegateLogger] backed by Kermit
 */
public actual fun createDefaultLogger(): Logger =
    KermitDelegateLogger(KermitLoggerImpl.withTag("EduGo"))

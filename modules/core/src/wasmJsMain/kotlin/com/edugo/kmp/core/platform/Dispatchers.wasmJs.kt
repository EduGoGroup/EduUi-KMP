package com.edugo.kmp.core.platform

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * WasmJS implementation of AppDispatchers.
 *
 * In WebAssembly/JavaScript, all dispatchers use the same event loop since it is single-threaded.
 * All dispatchers map to Dispatchers.Default which uses the JS event loop.
 */
public actual object AppDispatchers {
    actual val Main: CoroutineDispatcher = Dispatchers.Default

    actual val IO: CoroutineDispatcher = Dispatchers.Default

    actual val Default: CoroutineDispatcher = Dispatchers.Default
}

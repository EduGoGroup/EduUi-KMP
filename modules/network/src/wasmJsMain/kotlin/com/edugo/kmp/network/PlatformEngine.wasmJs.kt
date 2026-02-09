package com.edugo.kmp.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.js.Js

/**
 * WasmJs implementation using Js engine (Fetch API).
 */
public actual fun createPlatformEngine(): HttpClientEngine = Js.create()

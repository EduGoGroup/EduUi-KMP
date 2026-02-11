package com.edugo.kmp.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin

/**
 * iOS implementation using Darwin engine (URLSession).
 *
 * Darwin engine provee:
 * - Integración nativa con URLSession de Apple
 * - Soporte ATS (App Transport Security)
 * - HTTP/2 automatico
 * - Manejo de certificados del sistema
 */
public actual fun createPlatformEngine(): HttpClientEngine = Darwin.create()

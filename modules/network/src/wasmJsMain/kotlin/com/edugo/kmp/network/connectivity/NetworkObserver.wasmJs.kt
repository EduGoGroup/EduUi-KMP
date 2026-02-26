@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.edugo.kmp.network.connectivity

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * WasmJS implementation using navigator.onLine and online/offline event listeners.
 */
internal class WasmJsNetworkObserver : NetworkObserver {

    private val _status = MutableStateFlow(NetworkStatus.UNAVAILABLE)
    override val status: StateFlow<NetworkStatus> = _status.asStateFlow()

    private var listening = false

    override fun start() {
        if (listening) return
        listening = true

        _status.value = if (isNavigatorOnline()) NetworkStatus.AVAILABLE else NetworkStatus.UNAVAILABLE

        setupListeners(
            onOnline = { _status.value = NetworkStatus.AVAILABLE },
            onOffline = { _status.value = NetworkStatus.UNAVAILABLE }
        )
    }

    override fun stop() {
        if (!listening) return
        listening = false
        teardownListeners()
    }
}

private fun isNavigatorOnline(): Boolean = js("navigator.onLine")

private fun setupListeners(onOnline: () -> Unit, onOffline: () -> Unit): Unit = js(
    """{
        window.__edugo_online = onOnline;
        window.__edugo_offline = onOffline;
        window.addEventListener('online', window.__edugo_online);
        window.addEventListener('offline', window.__edugo_offline);
    }"""
)

private fun teardownListeners(): Unit = js(
    """{
        if (window.__edugo_online) window.removeEventListener('online', window.__edugo_online);
        if (window.__edugo_offline) window.removeEventListener('offline', window.__edugo_offline);
        window.__edugo_online = null;
        window.__edugo_offline = null;
    }"""
)

public actual fun createNetworkObserver(): NetworkObserver = WasmJsNetworkObserver()

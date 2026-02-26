package com.edugo.kmp.network.connectivity

import kotlinx.coroutines.flow.StateFlow

/**
 * Represents the current network connectivity status.
 */
public enum class NetworkStatus {
    AVAILABLE,
    UNAVAILABLE,
    LOSING
}

/**
 * Observes network connectivity changes across platforms.
 *
 * Each platform provides an optimized implementation:
 * - **Android**: ConnectivityManager.registerDefaultNetworkCallback
 * - **JVM/Desktop**: Coroutine-based HTTP health-check polling
 * - **iOS**: NWPathMonitor
 * - **WasmJS**: navigator.onLine + online/offline events
 */
public interface NetworkObserver {
    /** Current network status as a [StateFlow]. */
    public val status: StateFlow<NetworkStatus>

    /** Convenience property derived from [status]. */
    public val isOnline: Boolean
        get() = status.value == NetworkStatus.AVAILABLE

    /** Start observing network changes. */
    public fun start()

    /** Stop observing network changes and release resources. */
    public fun stop()
}

/**
 * Creates a platform-specific [NetworkObserver] instance.
 */
public expect fun createNetworkObserver(): NetworkObserver

package com.edugo.kmp.network.connectivity

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL

/**
 * JVM/Desktop implementation using HTTP health-check polling.
 *
 * Performs a lightweight HEAD request every [pollIntervalMs] to detect connectivity.
 */
internal class DesktopNetworkObserver(
    private val pollIntervalMs: Long = 30_000L,
    private val healthCheckUrl: String = "https://clients3.google.com/generate_204"
) : NetworkObserver {

    private val _status = MutableStateFlow(NetworkStatus.AVAILABLE)
    override val status: StateFlow<NetworkStatus> = _status.asStateFlow()

    private var scope: CoroutineScope? = null
    private var pollJob: Job? = null

    init {
        start()
    }

    override fun start() {
        if (scope != null) return

        val newScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        scope = newScope

        pollJob = newScope.launch {
            // Immediate first check
            val reachable = checkConnectivity()
            _status.value = if (reachable) NetworkStatus.AVAILABLE else NetworkStatus.UNAVAILABLE
            while (isActive) {
                delay(pollIntervalMs)
                val check = checkConnectivity()
                _status.value = if (check) NetworkStatus.AVAILABLE else NetworkStatus.UNAVAILABLE
            }
        }
    }

    override fun stop() {
        pollJob?.cancel()
        pollJob = null
        scope?.cancel()
        scope = null
    }

    private fun checkConnectivity(): Boolean {
        return try {
            val connection = URL(healthCheckUrl).openConnection() as HttpURLConnection
            connection.requestMethod = "HEAD"
            connection.connectTimeout = 5_000
            connection.readTimeout = 5_000
            connection.useCaches = false
            val code = connection.responseCode
            connection.disconnect()
            code in 200..399
        } catch (_: Exception) {
            false
        }
    }
}

public actual fun createNetworkObserver(): NetworkObserver = DesktopNetworkObserver()

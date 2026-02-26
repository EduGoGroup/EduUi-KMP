package com.edugo.kmp.network.connectivity

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Network.nw_path_get_status
import platform.Network.nw_path_monitor_cancel
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_monitor_t
import platform.Network.nw_path_status_satisfied
import platform.Network.nw_path_status_satisfiable
import platform.darwin.dispatch_get_main_queue

/**
 * iOS implementation using NWPathMonitor from the Network framework.
 */
internal class IosNetworkObserver : NetworkObserver {

    private val _status = MutableStateFlow(NetworkStatus.UNAVAILABLE)
    override val status: StateFlow<NetworkStatus> = _status.asStateFlow()

    private var monitor: nw_path_monitor_t? = null

    override fun start() {
        if (monitor != null) return

        val pathMonitor = nw_path_monitor_create()
        monitor = pathMonitor

        nw_path_monitor_set_update_handler(pathMonitor) { path ->
            val pathStatus = nw_path_get_status(path)
            _status.value = when (pathStatus) {
                nw_path_status_satisfied -> NetworkStatus.AVAILABLE
                nw_path_status_satisfiable -> NetworkStatus.LOSING
                else -> NetworkStatus.UNAVAILABLE
            }
        }

        nw_path_monitor_set_queue(pathMonitor, dispatch_get_main_queue())
        nw_path_monitor_start(pathMonitor)
    }

    override fun stop() {
        monitor?.let { nw_path_monitor_cancel(it) }
        monitor = null
    }
}

public actual fun createNetworkObserver(): NetworkObserver = IosNetworkObserver()

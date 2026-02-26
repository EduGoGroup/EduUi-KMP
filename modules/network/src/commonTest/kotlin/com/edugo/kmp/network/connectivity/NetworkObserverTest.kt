package com.edugo.kmp.network.connectivity

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Fake implementation for testing components that depend on NetworkObserver.
 */
class TestNetworkObserver : NetworkObserver {
    private val _status = MutableStateFlow(NetworkStatus.UNAVAILABLE)
    override val status: StateFlow<NetworkStatus> = _status.asStateFlow()

    private var started = false

    override fun start() {
        started = true
    }

    override fun stop() {
        started = false
    }

    /** Simulate a network status change. */
    fun emit(newStatus: NetworkStatus) {
        _status.value = newStatus
    }

    val isStarted: Boolean get() = started
}

class NetworkObserverTest {

    @Test
    fun initialStatusIsUnavailable() {
        val observer = TestNetworkObserver()
        assertEquals(NetworkStatus.UNAVAILABLE, observer.status.value)
    }

    @Test
    fun isOnlineReturnsTrueWhenAvailable() {
        val observer = TestNetworkObserver()
        observer.emit(NetworkStatus.AVAILABLE)
        assertTrue(observer.isOnline)
    }

    @Test
    fun isOnlineReturnsFalseWhenUnavailable() {
        val observer = TestNetworkObserver()
        observer.emit(NetworkStatus.UNAVAILABLE)
        assertFalse(observer.isOnline)
    }

    @Test
    fun isOnlineReturnsFalseWhenLosing() {
        val observer = TestNetworkObserver()
        observer.emit(NetworkStatus.LOSING)
        assertFalse(observer.isOnline)
    }

    @Test
    fun statusTransitionsWorkCorrectly() {
        val observer = TestNetworkObserver()
        assertEquals(NetworkStatus.UNAVAILABLE, observer.status.value)

        observer.emit(NetworkStatus.AVAILABLE)
        assertEquals(NetworkStatus.AVAILABLE, observer.status.value)

        observer.emit(NetworkStatus.LOSING)
        assertEquals(NetworkStatus.LOSING, observer.status.value)

        observer.emit(NetworkStatus.UNAVAILABLE)
        assertEquals(NetworkStatus.UNAVAILABLE, observer.status.value)
    }

    @Test
    fun startAndStopTrackState() {
        val observer = TestNetworkObserver()
        assertFalse(observer.isStarted)

        observer.start()
        assertTrue(observer.isStarted)

        observer.stop()
        assertFalse(observer.isStarted)
    }
}

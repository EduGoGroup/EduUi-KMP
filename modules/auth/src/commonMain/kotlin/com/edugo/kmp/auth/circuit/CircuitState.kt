package com.edugo.kmp.auth.circuit

import kotlinx.datetime.Instant

public sealed class CircuitState {
    public object Closed : CircuitState()
    public data class Open(val openedAt: Instant) : CircuitState()
    public data class HalfOpen(val attempt: Int) : CircuitState()
}

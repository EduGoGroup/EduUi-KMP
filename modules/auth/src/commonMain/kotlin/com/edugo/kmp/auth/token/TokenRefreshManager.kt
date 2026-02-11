package com.edugo.kmp.auth.token

import com.edugo.kmp.auth.model.AuthToken
import com.edugo.kmp.foundation.result.Result
import kotlinx.coroutines.flow.Flow

/**
 * Manager que orquestra el refresh de tokens con sincronización, retry y manejo de errores robusto.
 */
public interface TokenRefreshManager {

    public suspend fun refreshIfNeeded(): Result<AuthToken>

    public suspend fun forceRefresh(): Result<AuthToken>

    public fun shouldRefresh(token: AuthToken): Boolean

    public val onRefreshFailed: Flow<RefreshFailureReason>

    public val onRefreshSuccess: Flow<AuthToken>

    public fun startAutomaticRefresh(token: AuthToken)

    public fun stopAutomaticRefresh()

    public fun cancelPendingRefresh()
}

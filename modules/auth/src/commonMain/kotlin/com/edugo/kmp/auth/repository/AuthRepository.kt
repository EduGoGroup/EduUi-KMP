package com.edugo.kmp.auth.repository

import com.edugo.kmp.auth.model.AvailableContextsResponse
import com.edugo.kmp.auth.model.LoginCredentials
import com.edugo.kmp.auth.model.LoginResponse
import com.edugo.kmp.auth.model.SwitchContextResponse
import com.edugo.kmp.foundation.result.Result

/**
 * Repositorio de autenticacion que maneja las operaciones de red con el backend.
 */
public interface AuthRepository {
    public suspend fun login(credentials: LoginCredentials): Result<LoginResponse>
    public suspend fun logout(accessToken: String): Result<Unit>
    public suspend fun refresh(refreshToken: String): Result<RefreshResponse>
    public suspend fun verifyToken(token: String): Result<TokenVerificationResponse>
    public suspend fun getAvailableContexts(accessToken: String): Result<AvailableContextsResponse>
    public suspend fun switchContext(accessToken: String, schoolId: String): Result<SwitchContextResponse>
}

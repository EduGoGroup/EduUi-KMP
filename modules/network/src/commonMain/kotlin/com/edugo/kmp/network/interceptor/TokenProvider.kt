package com.edugo.kmp.network.interceptor

/**
 * Proveedor de tokens de autenticación.
 */
public interface TokenProvider {
    /**
     * Obtiene el token actual. Retorna null si no hay sesión.
     */
    public suspend fun getToken(): String?

    /**
     * Refresca el token. Retorna nuevo token o null si falla.
     */
    public suspend fun refreshToken(): String?

    /**
     * Indica si el token actual ha expirado.
     */
    public suspend fun isTokenExpired(): Boolean
}

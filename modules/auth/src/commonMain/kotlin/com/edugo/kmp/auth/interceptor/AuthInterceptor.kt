package com.edugo.kmp.auth.interceptor

import com.edugo.kmp.network.interceptor.Interceptor
import com.edugo.kmp.network.interceptor.TokenProvider
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * Interceptor que agrega header Authorization con Bearer token.
 *
 * Vive en kmp-auth, usa interfaces de kmp-network.
 */
public class AuthInterceptor(
    private val tokenProvider: TokenProvider,
    private val autoRefresh: Boolean = true,
    private val headerName: String = HttpHeaders.Authorization,
    private val tokenPrefix: String = "Bearer "
) : Interceptor {

    override val order: Int = 20

    override suspend fun interceptRequest(request: HttpRequestBuilder) {
        if (request.headers.contains(headerName)) {
            return
        }

        var token = tokenProvider.getToken()

        if (autoRefresh && token != null && tokenProvider.isTokenExpired()) {
            token = tokenProvider.refreshToken()
        }

        token?.let {
            request.header(headerName, "$tokenPrefix$it")
        }
    }

    public companion object {
        public fun withStaticToken(token: String): AuthInterceptor {
            return AuthInterceptor(
                tokenProvider = object : TokenProvider {
                    override suspend fun getToken(): String = token
                    override suspend fun refreshToken(): String = token
                    override suspend fun isTokenExpired(): Boolean = false
                },
                autoRefresh = false
            )
        }
    }
}

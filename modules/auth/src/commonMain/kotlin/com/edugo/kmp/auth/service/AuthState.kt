package com.edugo.kmp.auth.service

import com.edugo.kmp.auth.model.AuthToken
import com.edugo.kmp.auth.model.AuthUserInfo

/**
 * Sealed class que representa el estado de autenticación del usuario.
 */
public sealed class AuthState {

    public data class Authenticated(
        val user: AuthUserInfo,
        val token: AuthToken
    ) : AuthState() {
        public fun isTokenExpired(): Boolean = token.isExpired()

        public fun canRefresh(): Boolean = token.hasRefreshToken()
    }

    public object Unauthenticated : AuthState()

    public object Loading : AuthState()
}

public val AuthState.isAuthenticated: Boolean
    get() = this is AuthState.Authenticated

public val AuthState.isUnauthenticated: Boolean
    get() = this is AuthState.Unauthenticated

public val AuthState.isLoading: Boolean
    get() = this is AuthState.Loading

public val AuthState.currentUser: AuthUserInfo?
    get() = (this as? AuthState.Authenticated)?.user

public val AuthState.currentToken: AuthToken?
    get() = (this as? AuthState.Authenticated)?.token

public val AuthState.currentUserId: String?
    get() = currentUser?.id

public val AuthState.currentUserEmail: String?
    get() = currentUser?.email

public val AuthState.currentUserRole: String?
    get() = currentUser?.role

public val AuthState.isTokenExpired: Boolean
    get() = (this as? AuthState.Authenticated)?.isTokenExpired() ?: false

public val AuthState.canRefreshToken: Boolean
    get() = (this as? AuthState.Authenticated)?.canRefresh() ?: false

public inline fun AuthState.ifAuthenticated(action: (AuthUserInfo, AuthToken) -> Unit) {
    if (this is AuthState.Authenticated) {
        action(user, token)
    }
}

public inline fun AuthState.ifUnauthenticated(action: () -> Unit) {
    if (this is AuthState.Unauthenticated) {
        action()
    }
}

public inline fun AuthState.ifLoading(action: () -> Unit) {
    if (this is AuthState.Loading) {
        action()
    }
}

public inline fun <R> AuthState.fold(
    onAuthenticated: (AuthUserInfo, AuthToken) -> R,
    onUnauthenticated: () -> R,
    onLoading: () -> R
): R {
    return when (this) {
        is AuthState.Authenticated -> onAuthenticated(user, token)
        is AuthState.Unauthenticated -> onUnauthenticated()
        is AuthState.Loading -> onLoading()
    }
}

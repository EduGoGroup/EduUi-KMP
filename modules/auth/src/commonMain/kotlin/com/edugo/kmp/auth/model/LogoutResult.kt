package com.edugo.kmp.auth.model

/**
 * Resultado del proceso de logout.
 *
 * Distingue entre logout completo, parcial (offline) e idempotente.
 */
public sealed class LogoutResult {
    /**
     * Logout exitoso: local y remoto completados.
     */
    public object Success : LogoutResult()

    /**
     * Logout local exitoso, remoto falló (offline o error de red).
     *
     * @property remoteError Descripción del error al intentar notificar al backend
     */
    public data class PartialSuccess(
        val remoteError: String?
    ) : LogoutResult()

    /**
     * Ya no había sesión activa (llamada idempotente).
     */
    public object AlreadyLoggedOut : LogoutResult()
}

public val LogoutResult.isSuccess: Boolean
    get() = this is LogoutResult.Success

public val LogoutResult.localCleared: Boolean
    get() = this is LogoutResult.Success || this is LogoutResult.PartialSuccess

public val LogoutResult.isPartial: Boolean
    get() = this is LogoutResult.PartialSuccess

public val LogoutResult.wasAlreadyLoggedOut: Boolean
    get() = this is LogoutResult.AlreadyLoggedOut

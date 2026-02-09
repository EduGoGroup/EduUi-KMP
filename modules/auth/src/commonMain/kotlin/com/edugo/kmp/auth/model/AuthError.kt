package com.edugo.kmp.auth.model

import com.edugo.kmp.foundation.error.ErrorCode

/**
 * Sealed class que representa los diferentes tipos de errores de autenticación.
 *
 * Cada error está mapeado a su correspondiente [ErrorCode] para consistencia
 * con el sistema de errores del proyecto.
 */
public sealed class AuthError {
    /**
     * Código de error asociado del sistema.
     */
    public abstract val errorCode: ErrorCode

    /**
     * Credenciales inválidas (401 Unauthorized).
     */
    public object InvalidCredentials : AuthError() {
        override val errorCode: ErrorCode = ErrorCode.AUTH_INVALID_CREDENTIALS
    }

    /**
     * Usuario no encontrado (404 Not Found).
     */
    public object UserNotFound : AuthError() {
        override val errorCode: ErrorCode = ErrorCode.BUSINESS_RESOURCE_NOT_FOUND
    }

    /**
     * Cuenta bloqueada (423 Locked).
     */
    public object AccountLocked : AuthError() {
        override val errorCode: ErrorCode = ErrorCode.AUTH_ACCOUNT_LOCKED
    }

    /**
     * Usuario inactivo (403 Forbidden).
     */
    public object UserInactive : AuthError() {
        override val errorCode: ErrorCode = ErrorCode.AUTH_FORBIDDEN
    }

    /**
     * Error de red.
     *
     * @property cause Descripción del error de red
     */
    public data class NetworkError(val cause: String) : AuthError() {
        override val errorCode: ErrorCode = when {
            cause.contains("timeout", ignoreCase = true) -> ErrorCode.NETWORK_TIMEOUT
            cause.contains("connection", ignoreCase = true) -> ErrorCode.NETWORK_NO_CONNECTION
            cause.contains("dns", ignoreCase = true) -> ErrorCode.NETWORK_DNS_FAILURE
            else -> ErrorCode.NETWORK_SERVER_ERROR
        }
    }

    /**
     * Error desconocido.
     *
     * @property message Descripción del error
     */
    public data class UnknownError(val message: String) : AuthError() {
        override val errorCode: ErrorCode = ErrorCode.SYSTEM_UNKNOWN_ERROR
    }

    /**
     * Obtiene un mensaje amigable para el usuario.
     */
    public fun getUserFriendlyMessage(): String = ERROR_MESSAGES[this::class] ?:
        "Ocurrió un error inesperado. Por favor intenta de nuevo más tarde."

    companion object {
        private val ERROR_MESSAGES = mapOf(
            InvalidCredentials::class to
                "Usuario o contraseña incorrectos. Por favor verifica tus credenciales.",
            UserNotFound::class to
                "No encontramos una cuenta con ese correo electrónico.",
            AccountLocked::class to
                "Tu cuenta ha sido bloqueada. Por favor contacta al soporte.",
            UserInactive::class to
                "Tu cuenta está inactiva. Por favor contacta al administrador.",
            NetworkError::class to
                "Problema de conexión. Verifica tu internet e intenta de nuevo.",
            UnknownError::class to
                "Ocurrió un error inesperado. Por favor intenta de nuevo más tarde."
        )

        /**
         * Crea un AuthError desde un código de error del backend.
         */
        public fun fromErrorResponse(code: String, message: String): AuthError {
            return when (code.uppercase()) {
                "AUTH_INVALID_CREDENTIALS" -> InvalidCredentials
                "BUSINESS_RESOURCE_NOT_FOUND" -> UserNotFound
                "AUTH_ACCOUNT_LOCKED" -> AccountLocked
                "AUTH_FORBIDDEN", "AUTH_USER_INACTIVE" -> UserInactive
                "NETWORK_TIMEOUT", "NETWORK_NO_CONNECTION", "NETWORK_DNS_FAILURE",
                "NETWORK_CONNECTION_RESET", "NETWORK_SERVER_ERROR" -> NetworkError(message)
                else -> UnknownError(message)
            }
        }

        /**
         * Crea un AuthError desde un HTTP status code.
         */
        public fun fromHttpStatus(statusCode: Int, message: String? = null): AuthError {
            return when (statusCode) {
                401 -> InvalidCredentials
                404 -> UserNotFound
                423 -> AccountLocked
                403 -> UserInactive
                in 500..599 -> UnknownError(message ?: "Server error")
                else -> UnknownError(message ?: "Authentication error")
            }
        }

        /**
         * Intenta parsear un AuthError desde un mensaje de error.
         */
        public fun fromMessage(message: String): AuthError {
            return when {
                message.contains("invalid credentials", ignoreCase = true) ||
                message.contains("incorrect", ignoreCase = true) ||
                message.contains("wrong password", ignoreCase = true) -> InvalidCredentials

                message.contains("not found", ignoreCase = true) ||
                message.contains("does not exist", ignoreCase = true) -> UserNotFound

                message.contains("locked", ignoreCase = true) ||
                message.contains("blocked", ignoreCase = true) -> AccountLocked

                message.contains("inactive", ignoreCase = true) ||
                message.contains("disabled", ignoreCase = true) -> UserInactive

                message.contains("network", ignoreCase = true) ||
                message.contains("connection", ignoreCase = true) ||
                message.contains("timeout", ignoreCase = true) -> NetworkError(message)

                else -> UnknownError(message)
            }
        }
    }
}

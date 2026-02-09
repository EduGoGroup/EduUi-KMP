package com.edugo.kmp.auth.model

import com.edugo.kmp.foundation.result.Result

/**
 * Sealed class que representa el resultado de una operación de login.
 */
public sealed class LoginResult {
    /**
     * Representa un login exitoso.
     *
     * @property response Respuesta del backend con tokens y datos del usuario
     */
    public data class Success(val response: LoginResponse) : LoginResult()

    /**
     * Representa un login fallido.
     *
     * @property error Error de autenticación que describe la causa de la falla
     */
    public data class Error(val error: AuthError) : LoginResult()

    public fun isSuccess(): Boolean = this is Success

    public fun isError(): Boolean = this is Error

    public fun getOrNull(): LoginResponse? = when (this) {
        is Success -> response
        is Error -> null
    }

    public fun getErrorOrNull(): AuthError? = when (this) {
        is Success -> null
        is Error -> error
    }

    public inline fun map(transform: (LoginResponse) -> LoginResponse): LoginResult {
        return when (this) {
            is Success -> Success(transform(response))
            is Error -> this
        }
    }

    public inline fun <R> fold(
        onSuccess: (LoginResponse) -> R,
        onError: (AuthError) -> R
    ): R {
        return when (this) {
            is Success -> onSuccess(response)
            is Error -> onError(error)
        }
    }

    public inline fun onSuccess(action: (LoginResponse) -> Unit): LoginResult {
        if (this is Success) {
            action(response)
        }
        return this
    }

    public inline fun onError(action: (AuthError) -> Unit): LoginResult {
        if (this is Error) {
            action(error)
        }
        return this
    }

    /**
     * Convierte este LoginResult a Result<LoginResponse>.
     */
    public fun toResult(): Result<LoginResponse> {
        return when (this) {
            is Success -> Result.Success(response)
            is Error -> Result.Failure(error.getUserFriendlyMessage())
        }
    }

    companion object {
        public fun success(response: LoginResponse): LoginResult = Success(response)

        public fun error(error: AuthError): LoginResult = Error(error)

        public fun fromResult(result: Result<LoginResponse>): LoginResult {
            return when (result) {
                is Result.Success -> Success(result.data)
                is Result.Failure -> {
                    val error = AuthError.fromMessage(result.error)
                    Error(error)
                }
                is Result.Loading -> {
                    Error(AuthError.UnknownError("Unexpected loading state"))
                }
            }
        }
    }
}

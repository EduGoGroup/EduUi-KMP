package com.edugo.kmp.auth.model

import com.edugo.kmp.foundation.result.Result
import com.edugo.kmp.foundation.result.failure
import com.edugo.kmp.foundation.result.success
import com.edugo.kmp.foundation.entity.ValidatableModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Modelo que representa las credenciales de inicio de sesión.
 *
 * @property email Correo electrónico del usuario
 * @property password Contraseña del usuario
 */
@Serializable
public data class LoginCredentials(
    @SerialName("email")
    val email: String,

    @SerialName("password")
    val password: String
) : ValidatableModel {

    override fun validate(): Result<Unit> {
        return when {
            email.isBlank() ->
                failure("Email cannot be blank")

            !isValidEmail(email) ->
                failure("Email must be a valid email address")

            password.isBlank() ->
                failure("Password cannot be blank")

            password.length < MIN_PASSWORD_LENGTH ->
                failure("Password must be at least $MIN_PASSWORD_LENGTH characters long")

            else ->
                success(Unit)
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val trimmed = email.trim()
        if (trimmed.isEmpty()) return false

        val atCount = trimmed.count { it == '@' }
        if (atCount != 1) return false

        val parts = trimmed.split("@")
        if (parts.size != 2) return false

        val localPart = parts[0]
        val domainPart = parts[1]

        if (localPart.isEmpty()) return false
        if (domainPart.isEmpty()) return false
        if (!domainPart.contains(".")) return false

        val domainParts = domainPart.split(".")
        if (domainParts.any { it.isEmpty() }) return false

        return true
    }

    /**
     * Obtiene una representación segura para logging.
     */
    public fun toLogString(): String {
        return "LoginCredentials(email=$email, password=***)"
    }

    companion object {
        public const val MIN_PASSWORD_LENGTH: Int = 8

        /**
         * Crea credenciales de ejemplo para tests.
         */
        public fun createTestCredentials(
            email: String = "test@edugo.com",
            password: String = "password123"
        ): LoginCredentials {
            return LoginCredentials(
                email = email,
                password = password
            )
        }
    }
}

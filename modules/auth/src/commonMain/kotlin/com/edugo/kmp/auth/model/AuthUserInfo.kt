package com.edugo.kmp.auth.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Modelo que representa la información del usuario autenticado.
 *
 * @property id Identificador único del usuario en el sistema
 * @property email Correo electrónico del usuario
 * @property firstName Nombre del usuario
 * @property lastName Apellido del usuario
 * @property fullName Nombre completo del usuario
 * @property role Rol del usuario en el sistema
 * @property schoolId Identificador de la escuela (nullable)
 */
@Serializable
public data class AuthUserInfo(
    @SerialName("id")
    val id: String,

    @SerialName("email")
    val email: String,

    @SerialName("first_name")
    val firstName: String,

    @SerialName("last_name")
    val lastName: String,

    @SerialName("full_name")
    val fullName: String,

    @SerialName("role")
    val role: String,

    @SerialName("school_id")
    val schoolId: String? = null
) {

    /**
     * Verifica si el usuario tiene un rol específico.
     *
     * @param roleName Nombre del rol a verificar
     * @return true si el usuario tiene ese rol
     */
    public fun hasRole(roleName: String): Boolean {
        return role.equals(roleName, ignoreCase = true)
    }

    /**
     * Verifica si el usuario está asociado a una escuela.
     */
    public fun hasSchool(): Boolean = !schoolId.isNullOrBlank()

    /**
     * Obtiene las iniciales del usuario.
     *
     * @return Iniciales del usuario (máximo 2 caracteres)
     */
    public fun getInitials(): String {
        val first = firstName.firstOrNull()?.uppercase() ?: ""
        val last = lastName.firstOrNull()?.uppercase() ?: ""
        return "$first$last"
    }

    /**
     * Obtiene el nombre para mostrar en UI.
     *
     * @return Nombre para mostrar
     */
    public fun getDisplayName(): String {
        return when {
            fullName.isNotBlank() -> fullName
            firstName.isNotBlank() && lastName.isNotBlank() -> "$firstName $lastName"
            firstName.isNotBlank() -> firstName
            lastName.isNotBlank() -> lastName
            else -> email
        }
    }

    /**
     * Obtiene una representación segura para logging.
     */
    public fun toLogString(): String {
        return "AuthUserInfo(id=$id, email=$email, role=$role, hasSchool=${hasSchool()})"
    }

    companion object {
        /**
         * Crea un usuario de ejemplo para tests.
         */
        public fun createTestUser(
            id: String = "test-user-123",
            email: String = "test@edugo.com",
            firstName: String = "Test",
            lastName: String = "User",
            role: String = "student",
            schoolId: String? = "test-school-456"
        ): AuthUserInfo {
            return AuthUserInfo(
                id = id,
                email = email,
                firstName = firstName,
                lastName = lastName,
                fullName = "$firstName $lastName",
                role = role,
                schoolId = schoolId
            )
        }
    }
}

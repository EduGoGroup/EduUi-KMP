package com.edugo.kmp.auth.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Modelo que representa el contexto RBAC activo del usuario.
 *
 * Este contexto incluye el rol actual, escuela, unidad académica y permisos
 * del usuario en el sistema. El usuario puede tener múltiples contextos
 * (por ej. profesor en una escuela, admin en otra) y este representa el activo.
 *
 * @property roleId ID del rol en el sistema
 * @property roleName Nombre del rol (ej: "student", "teacher", "admin", "super_admin")
 * @property schoolId ID de la escuela en este contexto
 * @property schoolName Nombre de la escuela
 * @property academicUnitId ID de la unidad académica (ej: un curso específico)
 * @property academicUnitName Nombre de la unidad académica
 * @property permissions Lista de permisos específicos del usuario en este contexto
 */
@Serializable
public data class UserContext(
    @SerialName("role_id")
    val roleId: String,

    @SerialName("role_name")
    val roleName: String,

    @SerialName("school_id")
    val schoolId: String? = null,

    @SerialName("school_name")
    val schoolName: String? = null,

    @SerialName("academic_unit_id")
    val academicUnitId: String? = null,

    @SerialName("academic_unit_name")
    val academicUnitName: String? = null,

    @SerialName("permissions")
    val permissions: List<String> = emptyList()
) {

    /**
     * Verifica si el usuario tiene un permiso específico.
     *
     * @param permission Nombre del permiso a verificar
     * @return true si el usuario tiene ese permiso
     */
    public fun hasPermission(permission: String): Boolean {
        return permissions.contains(permission)
    }

    /**
     * Verifica si el usuario tiene alguno de los permisos especificados.
     *
     * @param permissions Lista de permisos a verificar
     * @return true si el usuario tiene al menos uno de los permisos
     */
    public fun hasAnyPermission(vararg permissions: String): Boolean {
        return permissions.any { this.permissions.contains(it) }
    }

    /**
     * Verifica si el usuario tiene todos los permisos especificados.
     *
     * @param permissions Lista de permisos a verificar
     * @return true si el usuario tiene todos los permisos
     */
    public fun hasAllPermissions(vararg permissions: String): Boolean {
        return permissions.all { this.permissions.contains(it) }
    }

    /**
     * Verifica si el usuario tiene un rol específico.
     *
     * @param roleName Nombre del rol a verificar (case-insensitive)
     * @return true si el usuario tiene ese rol
     */
    public fun hasRole(roleName: String): Boolean {
        return this.roleName.equals(roleName, ignoreCase = true)
    }

    /**
     * Verifica si el usuario está en un contexto de escuela.
     *
     * @return true si el usuario tiene una escuela asignada
     */
    public fun hasSchool(): Boolean = !schoolId.isNullOrBlank()

    /**
     * Verifica si el usuario está en un contexto de unidad académica específica.
     *
     * @return true si el usuario tiene una unidad académica asignada
     */
    public fun hasAcademicUnit(): Boolean = !academicUnitId.isNullOrBlank()

    /**
     * Obtiene una representación segura para logging.
     */
    public fun toLogString(): String {
        return "UserContext(roleId=$roleId, roleName=$roleName, " +
                "schoolId=$schoolId, academicUnitId=$academicUnitId, " +
                "permissionsCount=${permissions.size})"
    }

    companion object {
        /**
         * Crea un contexto de ejemplo para tests.
         */
        public fun createTestContext(
            roleId: String = "role-123",
            roleName: String = "student",
            schoolId: String? = "school-456",
            schoolName: String? = "Test School",
            academicUnitId: String? = null,
            academicUnitName: String? = null,
            permissions: List<String> = listOf("materials.read", "progress.write")
        ): UserContext {
            return UserContext(
                roleId = roleId,
                roleName = roleName,
                schoolId = schoolId,
                schoolName = schoolName,
                academicUnitId = academicUnitId,
                academicUnitName = academicUnitName,
                permissions = permissions
            )
        }
    }
}

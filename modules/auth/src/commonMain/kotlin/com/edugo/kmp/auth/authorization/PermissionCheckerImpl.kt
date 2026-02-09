package com.edugo.kmp.auth.authorization

/**
 * Implementación de [PermissionChecker] basada en un resolver de permisos.
 *
 * El resolver es una función que dado un rol retorna sus permisos.
 * Esto permite que cada app defina su propia lógica de mapeo rol->permisos.
 *
 * Ejemplo:
 * ```kotlin
 * val checker = PermissionCheckerImpl<SystemRole, AppPermission> { role ->
 *     when (role) {
 *         SystemRole.STUDENT -> setOf(AppPermission.VIEW_GRADES)
 *         SystemRole.TEACHER -> setOf(AppPermission.VIEW_GRADES, AppPermission.EDIT_GRADES)
 *         SystemRole.ADMIN -> AppPermission.entries.toSet()
 *     }
 * }
 *
 * checker.hasPermission(SystemRole.TEACHER, AppPermission.EDIT_GRADES) // true
 * checker.hasPermission(SystemRole.STUDENT, AppPermission.EDIT_GRADES) // false
 * ```
 *
 * @param R Tipo concreto de Role
 * @param P Tipo concreto de Permission
 * @property permissionResolver Función que mapea roles a permisos
 */
public class PermissionCheckerImpl<R : Role, P : Permission>(
    private val permissionResolver: (R) -> Set<P>
) : PermissionChecker<R, P> {

    override fun hasPermission(role: R, permission: P): Boolean {
        return permission in getEffectivePermissions(role)
    }

    override fun hasAnyPermission(role: R, permissions: Set<P>): Boolean {
        val effective = getEffectivePermissions(role)
        return permissions.any { it in effective }
    }

    override fun hasAllPermissions(role: R, permissions: Set<P>): Boolean {
        val effective = getEffectivePermissions(role)
        return permissions.all { it in effective }
    }

    override fun getEffectivePermissions(role: R): Set<P> {
        return permissionResolver(role)
    }
}

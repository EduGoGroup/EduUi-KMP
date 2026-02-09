package com.edugo.kmp.auth.authorization

/**
 * Interface genérica para representar un rol en el sistema.
 *
 * Las apps consumidoras crean sus propios enums/sealed classes implementando esta interface.
 *
 * Ejemplo:
 * ```kotlin
 * enum class SystemRole(
 *     override val displayName: String,
 *     override val level: Int
 * ) : Role {
 *     STUDENT("Estudiante", 10),
 *     TEACHER("Profesor", 20),
 *     ADMIN("Administrador", 30);
 *
 *     override val name: String get() = this.toString()
 * }
 * ```
 */
public interface Role {
    public val name: String
    public val displayName: String
    public val level: Int
}

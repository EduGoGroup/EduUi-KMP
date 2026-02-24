package com.edugo.kmp.auth.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Respuesta del endpoint GET /api/v1/menu del IAM Platform.
 * Contiene el árbol de menú filtrado por permisos del usuario.
 */
@Serializable
public data class MenuResponse(
    @SerialName("items")
    val items: List<MenuItem> = emptyList()
)

/**
 * Item de menú en el árbol de navegación.
 * Puede contener hijos para formar una estructura jerárquica.
 *
 * @property key Identificador único del item (ej: "dashboard", "admin", "users")
 * @property displayName Nombre para mostrar en la UI
 * @property icon Nombre del icono (ej: "dashboard", "settings", "users")
 * @property scope Alcance del permiso: "system", "school", o "unit"
 * @property sortOrder Orden de presentación
 * @property permissions Lista de permisos asociados a este item
 * @property screens Mapa de screen_type → screen_key para navegar a pantallas SDUI
 * @property children Items hijos en el árbol
 */
@Serializable
public data class MenuItem(
    @SerialName("key")
    val key: String,

    @SerialName("display_name")
    val displayName: String,

    @SerialName("icon")
    val icon: String = "",

    @SerialName("scope")
    val scope: String = "school",

    @SerialName("sort_order")
    val sortOrder: Int = 0,

    @SerialName("permissions")
    val permissions: List<String> = emptyList(),

    @SerialName("screens")
    val screens: Map<String, String> = emptyMap(),

    @SerialName("children")
    val children: List<MenuItem> = emptyList()
) {
    /**
     * Verifica si este item tiene hijos.
     */
    public fun hasChildren(): Boolean = children.isNotEmpty()

    /**
     * Verifica si este item tiene pantallas asociadas.
     */
    public fun hasScreens(): Boolean = screens.isNotEmpty()

    /**
     * Obtiene la pantalla por defecto (primera en el mapa de screens).
     */
    public fun getDefaultScreen(): String? = screens.values.firstOrNull()

    /**
     * Obtiene la pantalla para un tipo específico.
     */
    public fun getScreenForType(screenType: String): String? = screens[screenType]

    /**
     * Obtiene todos los items de forma plana (este item + todos los descendientes).
     */
    public fun flatten(): List<MenuItem> {
        return listOf(this) + children.flatMap { it.flatten() }
    }
}

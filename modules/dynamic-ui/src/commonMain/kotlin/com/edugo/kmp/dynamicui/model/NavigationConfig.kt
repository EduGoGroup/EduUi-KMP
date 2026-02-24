package com.edugo.kmp.dynamicui.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NavigationConfig(
    val type: NavigationType = NavigationType.TOP_BAR,
    val title: String? = null,
    val showBack: Boolean = false,
    val actions: List<NavigationAction> = emptyList()
)

@Serializable
enum class NavigationType {
    @SerialName("top-bar") TOP_BAR,
    @SerialName("bottom-nav") BOTTOM_NAV,
    @SerialName("drawer") DRAWER,
    @SerialName("tabs") TABS
}

@Serializable
data class NavigationAction(
    val id: String,
    val icon: String? = null,
    val label: String? = null,
    val actionId: String? = null
)

@Serializable
data class NavigationDefinition(
    val bottomNav: List<NavigationItem> = emptyList(),
    val drawerItems: List<NavigationItem> = emptyList(),
    val version: Int = 1
)

@Serializable
data class NavigationItem(
    val key: String,
    val label: String,
    val icon: String? = null,
    val screenKey: String? = null,
    val sortOrder: Int = 0,
    val children: List<NavigationItem> = emptyList()
)

/** Recursively find a NavigationItem by its key. */
fun List<NavigationItem>.findByKey(key: String): NavigationItem? {
    for (item in this) {
        if (item.key == key) return item
        val found = item.children.findByKey(key)
        if (found != null) return found
    }
    return null
}

/** Find the parent key of a child item by searching recursively. */
fun List<NavigationItem>.findParentKey(childKey: String): String? {
    for (item in this) {
        if (item.children.any { it.key == childKey }) return item.key
        val found = item.children.findParentKey(childKey)
        if (found != null) return found
    }
    return null
}

/** Return the first leaf item (no children) in a depth-first traversal. */
fun List<NavigationItem>.firstLeaf(): NavigationItem? {
    for (item in this) {
        if (item.children.isEmpty()) return item
        val leaf = item.children.firstLeaf()
        if (leaf != null) return leaf
    }
    return null
}

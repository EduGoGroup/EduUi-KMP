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
    val items: List<NavigationItem> = emptyList()
)

@Serializable
data class NavigationItem(
    val screenKey: String,
    val label: String,
    val icon: String? = null,
    val order: Int = 0
)

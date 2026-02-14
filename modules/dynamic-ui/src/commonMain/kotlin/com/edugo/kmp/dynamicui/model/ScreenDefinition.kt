package com.edugo.kmp.dynamicui.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class ScreenDefinition(
    val screenId: String,
    val screenKey: String,
    val screenName: String,
    val pattern: ScreenPattern,
    val version: Int,
    val template: ScreenTemplate,
    val dataEndpoint: String? = null,
    val dataConfig: DataConfig? = null,
    val actions: List<ActionDefinition> = emptyList(),
    val userPreferences: JsonObject? = null,
    val updatedAt: String
)

@Serializable
enum class ScreenPattern {
    @SerialName("login") LOGIN,
    @SerialName("form") FORM,
    @SerialName("list") LIST,
    @SerialName("dashboard") DASHBOARD,
    @SerialName("settings") SETTINGS,
    @SerialName("detail") DETAIL,
    @SerialName("search") SEARCH,
    @SerialName("profile") PROFILE,
    @SerialName("modal") MODAL,
    @SerialName("notification") NOTIFICATION,
    @SerialName("onboarding") ONBOARDING,
    @SerialName("empty-state") EMPTY_STATE
}

@Serializable
data class ScreenTemplate(
    val navigation: NavigationConfig? = null,
    val zones: List<Zone>,
    val platformOverrides: PlatformOverrides? = null
)

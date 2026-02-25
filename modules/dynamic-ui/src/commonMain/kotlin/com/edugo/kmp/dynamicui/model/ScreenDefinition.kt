package com.edugo.kmp.dynamicui.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class ScreenDefinition(
    @SerialName("screen_id") val screenId: String = "",
    @SerialName("screen_key") val screenKey: String,
    @SerialName("screen_name") val screenName: String,
    val pattern: ScreenPattern,
    val version: Int = 1,
    @SerialName("template") val template: ScreenTemplate,
    @SerialName("slot_data") val slotData: JsonObject? = null,
    @SerialName("data_config") val dataConfig: DataConfig? = null,
    @SerialName("handler_key") val handlerKey: String? = null,
    @SerialName("user_preferences") val userPreferences: JsonObject? = null,
    @SerialName("updated_at") val updatedAt: String = ""
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

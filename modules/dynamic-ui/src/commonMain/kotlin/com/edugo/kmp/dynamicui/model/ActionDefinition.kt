package com.edugo.kmp.dynamicui.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class ActionDefinition(
    val id: String,
    val trigger: ActionTrigger,
    val triggerSlotId: String? = null,
    val type: ActionType,
    val config: JsonObject = JsonObject(emptyMap())
)

@Serializable
enum class ActionTrigger {
    @SerialName("button_click") BUTTON_CLICK,
    @SerialName("item_click") ITEM_CLICK,
    @SerialName("pull_refresh") PULL_REFRESH,
    @SerialName("fab_click") FAB_CLICK,
    @SerialName("swipe") SWIPE,
    @SerialName("long_press") LONG_PRESS
}

@Serializable
enum class ActionType {
    @SerialName("NAVIGATE") NAVIGATE,
    @SerialName("NAVIGATE_BACK") NAVIGATE_BACK,
    @SerialName("API_CALL") API_CALL,
    @SerialName("SUBMIT_FORM") SUBMIT_FORM,
    @SerialName("REFRESH") REFRESH,
    @SerialName("CONFIRM") CONFIRM,
    @SerialName("LOGOUT") LOGOUT
}

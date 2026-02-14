package com.edugo.kmp.dynamicui.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Slot(
    val id: String,
    val controlType: ControlType,
    val style: String? = null,
    val value: String? = null,
    val field: String? = null,
    val placeholder: String? = null,
    val label: String? = null,
    val icon: String? = null,
    val required: Boolean = false,
    val readOnly: Boolean = false,
    val width: String? = null,
    val weight: Float? = null
)

@Serializable
enum class ControlType {
    @SerialName("label") LABEL,
    @SerialName("text-input") TEXT_INPUT,
    @SerialName("email-input") EMAIL_INPUT,
    @SerialName("password-input") PASSWORD_INPUT,
    @SerialName("number-input") NUMBER_INPUT,
    @SerialName("search-bar") SEARCH_BAR,
    @SerialName("checkbox") CHECKBOX,
    @SerialName("switch") SWITCH,
    @SerialName("radio-group") RADIO_GROUP,
    @SerialName("select") SELECT,
    @SerialName("filled-button") FILLED_BUTTON,
    @SerialName("outlined-button") OUTLINED_BUTTON,
    @SerialName("text-button") TEXT_BUTTON,
    @SerialName("icon-button") ICON_BUTTON,
    @SerialName("icon") ICON,
    @SerialName("avatar") AVATAR,
    @SerialName("image") IMAGE,
    @SerialName("divider") DIVIDER,
    @SerialName("list-item") LIST_ITEM,
    @SerialName("list-item-navigation") LIST_ITEM_NAVIGATION,
    @SerialName("metric-card") METRIC_CARD,
    @SerialName("chip") CHIP,
    @SerialName("rating") RATING
}

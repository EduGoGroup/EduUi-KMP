package com.edugo.kmp.dynamicui.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Zone(
    val id: String,
    val type: ZoneType,
    val distribution: Distribution = Distribution.STACKED,
    val condition: String? = null,
    val slots: List<Slot> = emptyList(),
    val zones: List<Zone> = emptyList(),
    val itemLayout: ItemLayout? = null
)

@Serializable
enum class ZoneType {
    @SerialName("container") CONTAINER,
    @SerialName("form-section") FORM_SECTION,
    @SerialName("simple-list") SIMPLE_LIST,
    @SerialName("grouped-list") GROUPED_LIST,
    @SerialName("metric-grid") METRIC_GRID,
    @SerialName("action-group") ACTION_GROUP,
    @SerialName("card-list") CARD_LIST
}

@Serializable
enum class Distribution {
    @SerialName("stacked") STACKED,
    @SerialName("side-by-side") SIDE_BY_SIDE,
    @SerialName("grid") GRID,
    @SerialName("flow-row") FLOW_ROW
}

@Serializable
data class ItemLayout(
    val slots: List<Slot> = emptyList()
)

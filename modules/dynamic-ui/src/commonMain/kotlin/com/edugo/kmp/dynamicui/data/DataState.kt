package com.edugo.kmp.dynamicui.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class DataPage(
    val items: List<JsonObject>,
    val total: Int? = null,
    val hasMore: Boolean = false
)

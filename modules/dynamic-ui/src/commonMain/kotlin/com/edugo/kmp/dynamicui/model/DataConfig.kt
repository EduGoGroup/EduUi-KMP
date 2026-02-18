package com.edugo.kmp.dynamicui.model

import kotlinx.serialization.Serializable

@Serializable
data class DataConfig(
    val defaultParams: Map<String, String> = emptyMap(),
    val pagination: PaginationConfig? = null,
    val refreshInterval: Long? = null
)

@Serializable
data class PaginationConfig(
    val pageSize: Int = 20,
    val limitParam: String = "limit",
    val offsetParam: String = "offset"
)

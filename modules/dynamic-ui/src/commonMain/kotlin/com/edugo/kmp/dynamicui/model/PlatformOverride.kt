package com.edugo.kmp.dynamicui.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class PlatformOverrides(
    val android: JsonObject? = null,
    val ios: JsonObject? = null,
    val desktop: JsonObject? = null,
    val web: JsonObject? = null
)

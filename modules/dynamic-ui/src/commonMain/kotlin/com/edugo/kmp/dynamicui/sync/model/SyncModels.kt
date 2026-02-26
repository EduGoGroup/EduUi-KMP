package com.edugo.kmp.dynamicui.sync.model

import com.edugo.kmp.auth.model.MenuItem
import com.edugo.kmp.auth.model.UserContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/**
 * Full sync bundle response. Backend returns flat structure:
 * - menu: list of menu items (not wrapped in bucket)
 * - permissions: list of permission strings
 * - screens: map of key -> ScreenBundleEntry (backend ScreenBundle DTO)
 * - available_contexts: list of UserContext
 * - hashes: map of bucket key -> hash
 */
@Serializable
data class SyncBundleResponse(
    @SerialName("menu") val menu: List<MenuItem> = emptyList(),
    @SerialName("permissions") val permissions: List<String> = emptyList(),
    @SerialName("screens") val screens: Map<String, ScreenBundleEntry> = emptyMap(),
    @SerialName("available_contexts") val availableContexts: List<UserContext> = emptyList(),
    @SerialName("hashes") val hashes: Map<String, String> = emptyMap(),
)

/**
 * A screen entry within the sync bundle.
 * This matches the backend's ScreenBundle DTO directly.
 * Template and slot_data are raw JSON since ScreenDefinition expects different structure.
 */
@Serializable
data class ScreenBundleEntry(
    @SerialName("screen_key") val screenKey: String,
    @SerialName("screen_name") val screenName: String,
    @SerialName("pattern") val pattern: String,
    @SerialName("version") val version: Int = 1,
    @SerialName("template") val template: JsonObject? = null,
    @SerialName("slot_data") val slotData: JsonObject? = null,
    @SerialName("data_config") val dataConfig: JsonObject? = null,
    @SerialName("handler_key") val handlerKey: String? = null,
    @SerialName("updated_at") val updatedAt: String = "",
)

@Serializable
data class DeltaSyncRequest(
    @SerialName("hashes") val hashes: Map<String, String>,
)

/**
 * Delta sync response. Backend returns:
 * - changed: map of bucket key -> BucketData (with data + hash)
 * - unchanged: list of bucket keys that didn't change
 */
@Serializable
data class DeltaSyncResponse(
    @SerialName("changed") val changed: Map<String, BucketData> = emptyMap(),
    @SerialName("unchanged") val unchanged: List<String> = emptyList(),
)

@Serializable
data class BucketData(
    @SerialName("data") val data: JsonElement,
    @SerialName("hash") val hash: String,
)

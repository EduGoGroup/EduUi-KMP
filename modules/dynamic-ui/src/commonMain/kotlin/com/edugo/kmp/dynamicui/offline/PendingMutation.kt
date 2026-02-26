package com.edugo.kmp.dynamicui.offline

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class PendingMutation(
    val id: String,
    val endpoint: String,
    val method: String,
    val body: JsonObject,
    val createdAt: Long,
    val retryCount: Int = 0,
    val maxRetries: Int = 3,
    val status: MutationStatus = MutationStatus.PENDING,
    val entityUpdatedAt: String? = null,
)

@Serializable
enum class MutationStatus {
    PENDING, SYNCING, FAILED, CONFLICTED
}

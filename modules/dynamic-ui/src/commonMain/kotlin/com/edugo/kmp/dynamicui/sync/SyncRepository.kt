package com.edugo.kmp.dynamicui.sync

import com.edugo.kmp.dynamicui.sync.model.DeltaSyncRequest
import com.edugo.kmp.dynamicui.sync.model.DeltaSyncResponse
import com.edugo.kmp.dynamicui.sync.model.SyncBundleResponse
import com.edugo.kmp.foundation.result.Result
import com.edugo.kmp.network.EduGoHttpClient

interface SyncRepository {
    suspend fun getBundle(): Result<SyncBundleResponse>
    suspend fun deltaSync(hashes: Map<String, String>): Result<DeltaSyncResponse>
}

class SyncRepositoryImpl(
    private val httpClient: EduGoHttpClient,
    private val iamApiBaseUrl: String,
) : SyncRepository {

    override suspend fun getBundle(): Result<SyncBundleResponse> {
        return try {
            val response: SyncBundleResponse = httpClient.get("$iamApiBaseUrl/api/v1/sync/bundle")
            Result.Success(response)
        } catch (e: Exception) {
            Result.Failure(e.message ?: "Network error")
        }
    }

    override suspend fun deltaSync(hashes: Map<String, String>): Result<DeltaSyncResponse> {
        return try {
            val request = DeltaSyncRequest(hashes = hashes)
            val response: DeltaSyncResponse = httpClient.post(
                "$iamApiBaseUrl/api/v1/sync/delta",
                request,
            )
            Result.Success(response)
        } catch (e: Exception) {
            Result.Failure(e.message ?: "Network error")
        }
    }
}

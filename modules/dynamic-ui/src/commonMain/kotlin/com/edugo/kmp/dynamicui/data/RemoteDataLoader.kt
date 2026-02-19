package com.edugo.kmp.dynamicui.data

import com.edugo.kmp.dynamicui.model.DataConfig
import com.edugo.kmp.foundation.result.Result
import com.edugo.kmp.network.EduGoHttpClient
import com.edugo.kmp.network.HttpRequestConfig
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

/**
 * Loads data from APIs with dual-API routing support.
 *
 * Endpoint convention:
 * - "/v1/materials"         → mobile API (default, no prefix)
 * - "mobile:/v1/materials"  → mobile API (explicit)
 * - "admin:/v1/users"       → admin API
 */
class RemoteDataLoader(
    private val httpClient: EduGoHttpClient,
    private val mobileBaseUrl: String,
    private val adminBaseUrl: String
) : DataLoader {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun loadData(
        endpoint: String,
        config: DataConfig,
        params: Map<String, String>
    ): Result<DataPage> {
        val (baseUrl, path) = resolveEndpoint(endpoint)

        if (!path.startsWith("/") || path.contains("..") || path.contains("://")) {
            return Result.Failure("Invalid data endpoint: $path")
        }

        val configBuilder = HttpRequestConfig.builder()
        config.defaultParams.forEach { (key, value) ->
            configBuilder.queryParam(key, value)
        }
        params.forEach { (key, value) ->
            configBuilder.queryParam(key, value)
        }
        config.pagination?.let {
            configBuilder.queryParam(it.limitParam, it.pageSize.toString())
        }

        val url = "$baseUrl$path"

        val result = httpClient.getSafe<String>(
            url = url,
            config = configBuilder.build()
        )

        return when (result) {
            is Result.Success -> parseResponse(result.data, config)
            is Result.Failure -> Result.Failure(result.error)
            is Result.Loading -> Result.Loading
        }
    }

    /**
     * Resolves the endpoint prefix to a base URL.
     * "admin:/v1/users" → (adminBaseUrl, "/v1/users")
     * "mobile:/v1/materials" → (mobileBaseUrl, "/v1/materials")
     * "/v1/materials" → (mobileBaseUrl, "/v1/materials")  // default
     */
    private fun resolveEndpoint(endpoint: String): Pair<String, String> {
        return when {
            endpoint.startsWith("admin:") -> adminBaseUrl to endpoint.removePrefix("admin:")
            endpoint.startsWith("mobile:") -> mobileBaseUrl to endpoint.removePrefix("mobile:")
            else -> mobileBaseUrl to endpoint
        }
    }

    private fun parseResponse(body: String, config: DataConfig): Result<DataPage> {
        return try {
            val element = json.parseToJsonElement(body)
            val pageSize = config.pagination?.pageSize ?: 20

            when (element) {
                is JsonArray -> {
                    val items = element.jsonArray.mapNotNull { it as? JsonObject }
                    DataPage(
                        items = items,
                        total = items.size,
                        hasMore = items.size >= pageSize
                    ).let { Result.Success(it) }
                }
                is JsonObject -> {
                    val obj = element.jsonObject
                    val items = (obj["items"] ?: obj["data"])
                        ?.jsonArray?.map { it.jsonObject }
                        ?: emptyList()
                    val total = obj["total"]?.toString()?.toIntOrNull()
                    DataPage(
                        items = items,
                        total = total,
                        hasMore = total?.let { items.size < it } ?: (items.size >= pageSize)
                    ).let { Result.Success(it) }
                }
                else -> Result.Failure("Unexpected response format")
            }
        } catch (e: Exception) {
            Result.Failure("Failed to parse data: ${e.message}")
        }
    }
}

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
import kotlinx.serialization.json.jsonPrimitive

/**
 * Loads data from APIs with dual-API routing support.
 *
 * Endpoint convention:
 * - "/api/v1/materials"         → mobile API (default, no prefix)
 * - "mobile:/api/v1/materials"  → mobile API (explicit)
 * - "admin:/api/v1/users"       → admin API
 * - "iam:/api/v1/auth/contexts" → IAM Platform API
 */
class RemoteDataLoader(
    private val httpClient: EduGoHttpClient,
    private val mobileBaseUrl: String,
    private val adminBaseUrl: String,
    private val iamBaseUrl: String
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
     * "admin:/api/v1/users" → (adminBaseUrl, "/api/v1/users")
     * "mobile:/api/v1/materials" → (mobileBaseUrl, "/api/v1/materials")
     * "iam:/api/v1/auth/contexts" → (iamBaseUrl, "/api/v1/auth/contexts")
     * "/api/v1/materials" → (mobileBaseUrl, "/api/v1/materials")  // default
     */
    private fun resolveEndpoint(endpoint: String): Pair<String, String> {
        return when {
            endpoint.startsWith("admin:") -> adminBaseUrl to endpoint.removePrefix("admin:")
            endpoint.startsWith("mobile:") -> mobileBaseUrl to endpoint.removePrefix("mobile:")
            endpoint.startsWith("iam:") -> iamBaseUrl to endpoint.removePrefix("iam:")
            else -> mobileBaseUrl to endpoint
        }
    }

    override suspend fun submitData(
        endpoint: String,
        body: JsonObject,
        method: String
    ): Result<JsonObject?> {
        val (baseUrl, path) = resolveEndpoint(endpoint)

        if (!path.startsWith("/") || path.contains("..") || path.contains("://")) {
            return Result.Failure("Invalid data endpoint: $path")
        }

        val url = "$baseUrl$path"

        val result: Result<String> = when (method.uppercase()) {
            "PUT" -> httpClient.putSafe(url, body)
            else -> httpClient.postSafe(url, body)
        }

        return when (result) {
            is Result.Success -> {
                try {
                    val jsonElement = json.parseToJsonElement(result.data)
                    Result.Success(jsonElement as? JsonObject)
                } catch (_: Exception) {
                    Result.Success(null)
                }
            }
            is Result.Failure -> Result.Failure(result.error)
            is Result.Loading -> Result.Loading
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
                    // Try standard keys first, then find any array value as fallback
                    val itemsElement = obj["items"] ?: obj["data"]
                        ?: obj.values.firstOrNull { it is JsonArray }
                    val items = if (itemsElement != null) {
                        itemsElement.jsonArray.map { it.jsonObject }
                    } else if (obj.containsKey("id")) {
                        // Single entity response (e.g., GET /schools/{id})
                        listOf(obj)
                    } else {
                        emptyList()
                    }
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

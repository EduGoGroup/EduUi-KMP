package com.edugo.kmp.dynamicui.data

import com.edugo.kmp.dynamicui.model.DataConfig
import com.edugo.kmp.foundation.result.Result
import com.edugo.kmp.network.EduGoHttpClient
import com.edugo.kmp.network.HttpRequestConfig

class RemoteDataLoader(
    private val httpClient: EduGoHttpClient,
    private val baseUrl: String
) : DataLoader {

    override suspend fun loadData(
        endpoint: String,
        config: DataConfig,
        params: Map<String, String>
    ): Result<DataPage> {
        if (!endpoint.startsWith("/") || endpoint.contains("..") || endpoint.contains("://")) {
            return Result.Failure("Invalid data endpoint")
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
        return httpClient.getSafe(
            url = "$baseUrl$endpoint",
            config = configBuilder.build()
        )
    }
}

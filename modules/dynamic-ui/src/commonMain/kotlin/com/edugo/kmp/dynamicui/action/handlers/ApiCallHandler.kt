package com.edugo.kmp.dynamicui.action.handlers

import com.edugo.kmp.dynamicui.action.ActionContext
import com.edugo.kmp.dynamicui.action.ActionHandler
import com.edugo.kmp.dynamicui.action.ActionResult
import com.edugo.kmp.foundation.result.Result
import com.edugo.kmp.network.EduGoHttpClient
import com.edugo.kmp.network.HttpRequestConfig
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

class ApiCallHandler(
    private val httpClient: EduGoHttpClient,
    private val baseUrl: String
) : ActionHandler {

    override suspend fun execute(context: ActionContext): ActionResult {
        val endpoint = context.config["endpoint"]?.jsonPrimitive?.contentOrNull
            ?: return ActionResult.Error("Missing endpoint in api_call config")

        val method = context.config["method"]?.jsonPrimitive?.contentOrNull ?: "GET"

        val configBuilder = HttpRequestConfig.builder()
        context.fieldValues.forEach { (key, value) ->
            configBuilder.queryParam(key, value)
        }

        return when (method.uppercase()) {
            "GET" -> {
                val result = httpClient.getSafe<JsonObject>(
                    url = "$baseUrl$endpoint",
                    config = configBuilder.build()
                )
                when (result) {
                    is Result.Success -> ActionResult.Success(data = result.data)
                    is Result.Failure -> ActionResult.Error(result.error, retry = true)
                    is Result.Loading -> ActionResult.Error("Unexpected loading state")
                }
            }
            else -> ActionResult.Error("HTTP method $method not yet supported")
        }
    }
}

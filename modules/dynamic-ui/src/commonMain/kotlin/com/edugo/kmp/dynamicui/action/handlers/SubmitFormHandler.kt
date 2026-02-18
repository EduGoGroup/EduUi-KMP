package com.edugo.kmp.dynamicui.action.handlers

import com.edugo.kmp.dynamicui.action.ActionContext
import com.edugo.kmp.dynamicui.action.ActionHandler
import com.edugo.kmp.dynamicui.action.ActionResult
import com.edugo.kmp.foundation.result.Result
import com.edugo.kmp.network.EduGoHttpClient
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

class SubmitFormHandler(
    private val httpClient: EduGoHttpClient,
    private val baseUrl: String
) : ActionHandler {

    override suspend fun execute(context: ActionContext): ActionResult {
        val endpoint = context.config["endpoint"]?.jsonPrimitive?.contentOrNull
            ?: return ActionResult.Error("Missing endpoint in submit_form config")

        if (!isValidEndpoint(endpoint)) {
            return ActionResult.Error("Invalid endpoint: must start with / and not contain path traversal")
        }

        val body = JsonObject(
            context.fieldValues.mapValues { (_, value) -> JsonPrimitive(value) }
        )

        val result = httpClient.postSafe<JsonObject, JsonObject>(
            url = "$baseUrl$endpoint",
            body = body
        )

        return when (result) {
            is Result.Success -> ActionResult.Success(
                message = "Form submitted",
                data = result.data
            )
            is Result.Failure -> ActionResult.Error(result.error, retry = true)
            is Result.Loading -> ActionResult.Error("Unexpected loading state")
        }
    }

    private fun isValidEndpoint(endpoint: String): Boolean {
        return endpoint.startsWith("/") && !endpoint.contains("..") && !endpoint.contains("://")
    }
}

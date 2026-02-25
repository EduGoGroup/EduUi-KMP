package com.edugo.kmp.screens.dynamic.contracts

import com.edugo.kmp.dynamicui.contract.CustomEventHandler
import com.edugo.kmp.dynamicui.contract.EventContext
import com.edugo.kmp.dynamicui.contract.EventResult
import com.edugo.kmp.dynamicui.contract.ScreenContract
import com.edugo.kmp.dynamicui.contract.ScreenEvent
import com.edugo.kmp.dynamicui.data.DataLoader
import com.edugo.kmp.foundation.result.Result
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

class MaterialEditContract(
    private val dataLoader: DataLoader
) : ScreenContract {
    override val screenKey = "material-edit"
    override val resource = "materials"

    override fun endpointFor(event: ScreenEvent, context: EventContext): String? = when (event) {
        ScreenEvent.LOAD_DATA -> {
            val id = context.params["id"]
            if (id != null) "/api/v1/materials/$id" else null
        }
        else -> null
    }

    override fun customEventHandlers(): Map<String, CustomEventHandler> = mapOf(
        "submit-form" to SubmitFormHandler(dataLoader)
    )

    private class SubmitFormHandler(
        private val dataLoader: DataLoader
    ) : CustomEventHandler {
        override val eventId = "submit-form"
        override val requiredPermission: String? = "materials:update"

        override suspend fun execute(context: EventContext): EventResult {
            val title = context.fieldValues["title"] ?: ""
            val id = context.params["id"]
                ?: return EventResult.Error("Material ID is required for edit")

            if (title.isBlank()) {
                return EventResult.Error("Title is required")
            }

            val body = JsonObject(buildMap {
                put("title", JsonPrimitive(title))
                context.fieldValues["description"]?.let { put("description", JsonPrimitive(it)) }
                context.fieldValues["type"]?.let { put("type", JsonPrimitive(it)) }
                context.fieldValues["url"]?.let { put("url", JsonPrimitive(it)) }
            })

            return when (val result = dataLoader.submitData("/api/v1/materials/$id", body, "PUT")) {
                is Result.Success -> {
                    EventResult.NavigateTo("material-detail", mapOf("id" to id))
                }
                is Result.Failure -> EventResult.Error(result.error, retry = true)
                is Result.Loading -> EventResult.Error("Unexpected loading state")
            }
        }
    }
}

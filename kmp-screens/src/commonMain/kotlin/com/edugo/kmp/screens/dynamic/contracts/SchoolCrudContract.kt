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

/**
 * Contract for school create and edit screens.
 *
 * Instantiated twice in DI: once with screenKey "school-create" and once with "school-edit".
 */
class SchoolCrudContract(
    override val screenKey: String,
    private val dataLoader: DataLoader
) : ScreenContract {
    override val resource = "schools"

    private val isCreate: Boolean get() = screenKey == "school-create"

    override fun endpointFor(event: ScreenEvent, context: EventContext): String? = when (event) {
        ScreenEvent.LOAD_DATA -> {
            if (isCreate) null
            else {
                val id = context.params["id"]
                if (id != null) "admin:/api/v1/schools/$id" else null
            }
        }
        else -> null
    }

    override fun customEventHandlers(): Map<String, CustomEventHandler> = mapOf(
        "submit-form" to SubmitFormHandler(dataLoader, isCreate)
    )

    private class SubmitFormHandler(
        private val dataLoader: DataLoader,
        private val isCreate: Boolean
    ) : CustomEventHandler {
        override val eventId = "submit-form"
        override val requiredPermission: String? =
            if (isCreate) "schools:create" else "schools:update"

        override suspend fun execute(context: EventContext): EventResult {
            val name = context.fieldValues["name"] ?: ""

            if (name.isBlank()) {
                return EventResult.Error("School name is required")
            }

            val body = JsonObject(buildMap {
                put("name", JsonPrimitive(name))
                context.fieldValues["address"]?.let { put("address", JsonPrimitive(it)) }
                context.fieldValues["phone"]?.let { put("phone", JsonPrimitive(it)) }
            })

            val endpoint: String
            val method: String

            if (isCreate) {
                endpoint = "admin:/api/v1/schools"
                method = "POST"
            } else {
                val id = context.params["id"]
                    ?: return EventResult.Error("School ID is required for edit")
                endpoint = "admin:/api/v1/schools/$id"
                method = "PUT"
            }

            return when (val result = dataLoader.submitData(endpoint, body, method)) {
                is Result.Success -> {
                    val schoolId = result.data?.get("id")?.jsonPrimitive?.content
                        ?: context.params["id"] ?: ""
                    EventResult.NavigateTo("school-detail", mapOf("id" to schoolId))
                }
                is Result.Failure -> EventResult.Error(result.error, retry = true)
                is Result.Loading -> EventResult.Error("Unexpected loading state")
            }
        }
    }
}

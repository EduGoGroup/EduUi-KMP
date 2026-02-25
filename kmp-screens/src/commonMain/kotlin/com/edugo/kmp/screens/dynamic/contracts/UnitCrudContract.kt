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
 * Contract for academic unit create and edit screens.
 *
 * Instantiated twice in DI: once with screenKey "unit-create" and once with "unit-edit".
 * Requires a schoolId in context.params to resolve the nested resource path.
 */
class UnitCrudContract(
    override val screenKey: String,
    private val dataLoader: DataLoader
) : ScreenContract {
    override val resource = "academic_units"

    private val isCreate: Boolean get() = screenKey == "unit-create"

    override fun endpointFor(event: ScreenEvent, context: EventContext): String? {
        val schoolId = context.params["schoolId"] ?: return null
        return when (event) {
            ScreenEvent.LOAD_DATA -> {
                if (isCreate) null
                else {
                    val id = context.params["id"]
                    if (id != null) "admin:/api/v1/schools/$schoolId/units/$id" else null
                }
            }
            else -> null
        }
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
            if (isCreate) "academic_units:create" else "academic_units:update"

        override suspend fun execute(context: EventContext): EventResult {
            val name = context.fieldValues["name"] ?: ""
            val schoolId = context.params["schoolId"]
                ?: return EventResult.Error("School ID is required")

            if (name.isBlank()) {
                return EventResult.Error("Unit name is required")
            }

            val body = JsonObject(buildMap {
                put("name", JsonPrimitive(name))
                context.fieldValues["description"]?.let { put("description", JsonPrimitive(it)) }
            })

            val endpoint: String
            val method: String

            if (isCreate) {
                endpoint = "admin:/api/v1/schools/$schoolId/units"
                method = "POST"
            } else {
                val id = context.params["id"]
                    ?: return EventResult.Error("Unit ID is required for edit")
                endpoint = "admin:/api/v1/schools/$schoolId/units/$id"
                method = "PUT"
            }

            return when (val result = dataLoader.submitData(endpoint, body, method)) {
                is Result.Success -> {
                    val unitId = result.data?.get("id")?.jsonPrimitive?.content
                        ?: context.params["id"] ?: ""
                    EventResult.NavigateTo(
                        "units-list",
                        mapOf("schoolId" to schoolId, "id" to unitId)
                    )
                }
                is Result.Failure -> EventResult.Error(result.error, retry = true)
                is Result.Loading -> EventResult.Error("Unexpected loading state")
            }
        }
    }
}

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
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Contract for user create and edit screens.
 *
 * Instantiated twice in DI: once with screenKey "user-create" and once with "user-edit".
 */
class UserCrudContract(
    override val screenKey: String,
    private val dataLoader: DataLoader
) : ScreenContract {
    override val resource = "users"

    private val isCreate: Boolean get() = screenKey == "user-create"

    override fun endpointFor(event: ScreenEvent, context: EventContext): String? = when (event) {
        ScreenEvent.LOAD_DATA -> {
            if (isCreate) null
            else {
                val id = context.params["id"]
                if (id != null) "admin:/api/v1/users/$id" else null
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
            if (isCreate) "users:create" else "users:update"

        override suspend fun execute(context: EventContext): EventResult {
            val firstName = context.fieldValues["first_name"] ?: ""
            val lastName = context.fieldValues["last_name"] ?: ""
            val email = context.fieldValues["email"] ?: ""

            if (firstName.isBlank()) {
                return EventResult.Error("First name is required")
            }
            if (lastName.isBlank()) {
                return EventResult.Error("Last name is required")
            }
            if (email.isBlank()) {
                return EventResult.Error("Email is required")
            }

            val body = JsonObject(buildMap {
                put("first_name", JsonPrimitive(firstName))
                put("last_name", JsonPrimitive(lastName))
                put("email", JsonPrimitive(email))
                context.fieldValues["phone"]?.let { put("phone", JsonPrimitive(it)) }
            })

            val endpoint: String
            val method: String

            if (isCreate) {
                endpoint = "admin:/api/v1/users"
                method = "POST"
            } else {
                val id = context.params["id"]
                    ?: return EventResult.Error("User ID is required for edit")
                endpoint = "admin:/api/v1/users/$id"
                method = "PUT"
            }

            return when (val result = dataLoader.submitData(endpoint, body, method)) {
                is Result.Success -> {
                    val userId = result.data?.get("id")?.jsonPrimitive?.content
                        ?: context.params["id"] ?: ""
                    EventResult.NavigateTo("user-detail", mapOf("id" to userId))
                }
                is Result.Failure -> EventResult.Error(result.error, retry = true)
                is Result.Loading -> EventResult.Error("Unexpected loading state")
            }
        }
    }
}

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

class MembershipAddContract(
    private val dataLoader: DataLoader
) : ScreenContract {
    override val screenKey = "membership-add"
    override val resource = "memberships"

    override fun endpointFor(event: ScreenEvent, context: EventContext): String? = null

    override fun customEventHandlers(): Map<String, CustomEventHandler> = mapOf(
        "submit-form" to SubmitFormHandler(dataLoader)
    )

    private class SubmitFormHandler(
        private val dataLoader: DataLoader
    ) : CustomEventHandler {
        override val eventId = "submit-form"
        override val requiredPermission: String? = "memberships:create"

        override suspend fun execute(context: EventContext): EventResult {
            val userEmail = context.fieldValues["user_email"] ?: ""
            val role = context.fieldValues["role"] ?: ""

            if (userEmail.isBlank()) {
                return EventResult.Error("User email is required")
            }
            if (role.isBlank()) {
                return EventResult.Error("Role is required")
            }

            val body = JsonObject(buildMap {
                put("user_email", JsonPrimitive(userEmail))
                put("role", JsonPrimitive(role))
                context.params["unitId"]?.let { put("unit_id", JsonPrimitive(it)) }
            })

            return when (val result = dataLoader.submitData("admin:/api/v1/memberships", body, "POST")) {
                is Result.Success -> EventResult.NavigateTo("memberships-list")
                is Result.Failure -> EventResult.Error(result.error, retry = true)
                is Result.Loading -> EventResult.Error("Unexpected loading state")
            }
        }
    }
}

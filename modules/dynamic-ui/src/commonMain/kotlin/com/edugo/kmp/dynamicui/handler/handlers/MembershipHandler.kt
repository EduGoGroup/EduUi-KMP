package com.edugo.kmp.dynamicui.handler.handlers

import com.edugo.kmp.dynamicui.action.ActionContext
import com.edugo.kmp.dynamicui.action.ActionResult
import com.edugo.kmp.dynamicui.data.DataLoader
import com.edugo.kmp.dynamicui.handler.ScreenActionHandler
import com.edugo.kmp.dynamicui.model.ActionDefinition
import com.edugo.kmp.dynamicui.model.ActionType
import com.edugo.kmp.foundation.result.Result
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

/**
 * Handler para agregar miembros a una unidad academica.
 * POST admin:/v1/memberships -> navegar a memberships-list.
 */
class MembershipHandler(
    private val dataLoader: DataLoader
) : ScreenActionHandler {

    override val screenKeys = setOf("membership-add")

    override fun canHandle(action: ActionDefinition): Boolean {
        return action.type == ActionType.SUBMIT_FORM
    }

    override suspend fun handle(
        action: ActionDefinition,
        context: ActionContext
    ): ActionResult {
        val fieldValues = context.fieldValues

        val userEmail = fieldValues["user_email"]
        if (userEmail.isNullOrBlank()) {
            return ActionResult.Error(message = "User email is required")
        }

        val role = fieldValues["role"]
        if (role.isNullOrBlank()) {
            return ActionResult.Error(message = "Role is required")
        }

        val unitId = context.config["unitId"]?.jsonPrimitive?.content
            ?: return ActionResult.Error(message = "Unit ID not found")

        val body = JsonObject(mapOf(
            "user_email" to JsonPrimitive(userEmail),
            "role" to JsonPrimitive(role),
            "unit_id" to JsonPrimitive(unitId)
        ))

        return try {
            when (val result = dataLoader.submitData("admin:/v1/memberships", body)) {
                is Result.Success -> ActionResult.NavigateTo(
                    screenKey = "memberships-list",
                    params = mapOf("unitId" to unitId)
                )
                is Result.Failure -> ActionResult.Error(message = result.error)
                is Result.Loading -> ActionResult.Error(message = "Unexpected loading state")
            }
        } catch (e: Exception) {
            ActionResult.Error(message = e.message ?: "Failed to add member")
        }
    }
}

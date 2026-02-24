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
 * Handler para CRUD de usuarios.
 * Create: POST admin:/v1/users -> navegar a user-detail.
 * Edit: PUT admin:/v1/users/{id} -> navegar a user-detail.
 */
class UserCrudHandler(
    private val dataLoader: DataLoader
) : ScreenActionHandler {

    override val screenKeys = setOf("user-create", "user-edit")

    override fun canHandle(action: ActionDefinition): Boolean {
        return action.type == ActionType.SUBMIT_FORM
    }

    override suspend fun handle(
        action: ActionDefinition,
        context: ActionContext
    ): ActionResult {
        val fieldValues = context.fieldValues
        val firstName = fieldValues["first_name"]
        val lastName = fieldValues["last_name"]
        val email = fieldValues["email"]

        if (firstName.isNullOrBlank() || lastName.isNullOrBlank()) {
            return ActionResult.Error(message = "First name and last name are required")
        }
        if (email.isNullOrBlank()) {
            return ActionResult.Error(message = "Email is required")
        }

        val isCreate = context.screenKey == "user-create"

        val body = JsonObject(buildMap {
            put("first_name", JsonPrimitive(firstName))
            put("last_name", JsonPrimitive(lastName))
            put("email", JsonPrimitive(email))
            if (isCreate) {
                fieldValues["password"]?.let { put("password", JsonPrimitive(it)) }
            }
        })

        return try {
            if (isCreate) {
                when (val result = dataLoader.submitData("admin:/v1/users", body)) {
                    is Result.Success -> {
                        val userId = result.data?.get("id")?.jsonPrimitive?.content
                        if (userId != null) {
                            ActionResult.NavigateTo(
                                screenKey = "user-detail",
                                params = mapOf("id" to userId)
                            )
                        } else {
                            ActionResult.NavigateTo(screenKey = "users-list")
                        }
                    }
                    is Result.Failure -> ActionResult.Error(message = result.error)
                    is Result.Loading -> ActionResult.Error(message = "Unexpected loading state")
                }
            } else {
                val userId = context.selectedItemId
                    ?: context.config["id"]?.jsonPrimitive?.content
                    ?: return ActionResult.Error(message = "User ID not found")
                when (val result = dataLoader.submitData("admin:/v1/users/$userId", body, method = "PUT")) {
                    is Result.Success -> ActionResult.NavigateTo(
                        screenKey = "user-detail",
                        params = mapOf("id" to userId)
                    )
                    is Result.Failure -> ActionResult.Error(message = result.error)
                    is Result.Loading -> ActionResult.Error(message = "Unexpected loading state")
                }
            }
        } catch (e: Exception) {
            ActionResult.Error(message = e.message ?: "Operation failed")
        }
    }
}

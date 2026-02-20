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
 * Handler para crear y editar escuelas.
 * - school-create: POST admin:/v1/schools -> navegar a school-detail
 * - school-edit: PUT admin:/v1/schools/{id} -> navegar a school-detail
 */
class SchoolCrudHandler(
    private val dataLoader: DataLoader
) : ScreenActionHandler {

    override val screenKeys = setOf("school-create", "school-edit")

    override fun canHandle(action: ActionDefinition): Boolean {
        return action.type == ActionType.SUBMIT_FORM
    }

    override suspend fun handle(
        action: ActionDefinition,
        context: ActionContext
    ): ActionResult {
        val fieldValues = context.fieldValues

        val name = fieldValues["name"]
        if (name.isNullOrBlank()) {
            return ActionResult.Error(message = "School name is required")
        }

        val body = JsonObject(buildMap {
            put("name", JsonPrimitive(name))
            fieldValues["address"]?.let { put("address", JsonPrimitive(it)) }
            fieldValues["phone"]?.let { put("phone", JsonPrimitive(it)) }
            fieldValues["description"]?.let { put("description", JsonPrimitive(it)) }
        })

        val isEdit = context.screenKey == "school-edit"

        return try {
            if (isEdit) {
                val schoolId = context.selectedItemId
                    ?: context.config["id"]?.jsonPrimitive?.content
                    ?: return ActionResult.Error(message = "School ID not found")

                when (val result = dataLoader.submitData("admin:/v1/schools/$schoolId", body, method = "PUT")) {
                    is Result.Success -> ActionResult.NavigateTo(
                        screenKey = "school-detail",
                        params = mapOf("id" to schoolId)
                    )
                    is Result.Failure -> ActionResult.Error(message = result.error)
                    is Result.Loading -> ActionResult.Error(message = "Unexpected loading state")
                }
            } else {
                when (val result = dataLoader.submitData("admin:/v1/schools", body)) {
                    is Result.Success -> {
                        val schoolId = result.data?.get("id")?.jsonPrimitive?.content
                        if (schoolId != null) {
                            ActionResult.NavigateTo(
                                screenKey = "school-detail",
                                params = mapOf("id" to schoolId)
                            )
                        } else {
                            ActionResult.NavigateTo(screenKey = "schools-list")
                        }
                    }
                    is Result.Failure -> ActionResult.Error(message = result.error)
                    is Result.Loading -> ActionResult.Error(message = "Unexpected loading state")
                }
            }
        } catch (e: Exception) {
            ActionResult.Error(message = e.message ?: "Failed to save school")
        }
    }
}

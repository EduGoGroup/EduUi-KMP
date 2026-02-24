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
 * Handler para crear y editar unidades academicas.
 * Create: POST admin:/v1/schools/{schoolId}/units -> navegar a unit-detail.
 * Edit: PUT admin:/v1/schools/{schoolId}/units/{id} -> navegar a unit-detail.
 */
class UnitCrudHandler(
    private val dataLoader: DataLoader
) : ScreenActionHandler {

    override val screenKeys = setOf("unit-create", "unit-edit")

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
            return ActionResult.Error(message = "Unit name is required")
        }

        val body = JsonObject(buildMap {
            put("name", JsonPrimitive(name))
            fieldValues["grade_level"]?.let { put("grade_level", JsonPrimitive(it)) }
            fieldValues["description"]?.let { put("description", JsonPrimitive(it)) }
        })

        val isEdit = context.screenKey == "unit-edit"

        return try {
            if (isEdit) {
                val unitId = context.selectedItemId
                    ?: context.config["id"]?.jsonPrimitive?.content
                    ?: return ActionResult.Error(message = "Unit ID not found")

                val schoolId = context.config["schoolId"]?.jsonPrimitive?.content
                    ?: return ActionResult.Error(message = "School ID not found")

                when (val result = dataLoader.submitData("admin:/v1/schools/$schoolId/units/$unitId", body, method = "PUT")) {
                    is Result.Success -> ActionResult.NavigateTo(
                        screenKey = "unit-detail",
                        params = mapOf("schoolId" to schoolId, "id" to unitId)
                    )
                    is Result.Failure -> ActionResult.Error(message = result.error)
                    is Result.Loading -> ActionResult.Error(message = "Unexpected loading state")
                }
            } else {
                val schoolId = context.config["schoolId"]?.jsonPrimitive?.content
                    ?: return ActionResult.Error(message = "School ID not found")

                when (val result = dataLoader.submitData("admin:/v1/schools/$schoolId/units", body)) {
                    is Result.Success -> {
                        val unitId = result.data?.get("id")?.jsonPrimitive?.content
                        if (unitId != null) {
                            ActionResult.NavigateTo(
                                screenKey = "unit-detail",
                                params = mapOf("schoolId" to schoolId, "id" to unitId)
                            )
                        } else {
                            ActionResult.NavigateTo(screenKey = "units-list")
                        }
                    }
                    is Result.Failure -> ActionResult.Error(message = result.error)
                    is Result.Loading -> ActionResult.Error(message = "Unexpected loading state")
                }
            }
        } catch (e: Exception) {
            ActionResult.Error(message = e.message ?: "Failed to save unit")
        }
    }
}

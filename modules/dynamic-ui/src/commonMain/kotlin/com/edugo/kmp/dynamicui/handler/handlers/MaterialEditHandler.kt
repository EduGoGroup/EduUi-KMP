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

class MaterialEditHandler(
    private val dataLoader: DataLoader
) : ScreenActionHandler {

    override val screenKeys = setOf("material-edit")

    override fun canHandle(action: ActionDefinition): Boolean {
        return action.type == ActionType.SUBMIT_FORM
    }

    override suspend fun handle(
        action: ActionDefinition,
        context: ActionContext
    ): ActionResult {
        val fieldValues = context.fieldValues

        val title = fieldValues["title"]
        if (title.isNullOrBlank()) {
            return ActionResult.Error(message = "Title is required")
        }

        val materialId = context.selectedItemId
            ?: context.config["id"]?.jsonPrimitive?.content
            ?: return ActionResult.Error(message = "Material ID not found")

        val body = JsonObject(buildMap {
            put("title", JsonPrimitive(title))
            fieldValues["subject"]?.let { put("subject", JsonPrimitive(it)) }
            fieldValues["grade"]?.let { put("grade", JsonPrimitive(it)) }
            fieldValues["description"]?.let { put("description", JsonPrimitive(it)) }
        })

        return try {
            when (val result = dataLoader.submitData("/v1/materials/$materialId", body, method = "PUT")) {
                is Result.Success -> ActionResult.NavigateTo(
                    screenKey = "material-detail",
                    params = mapOf("id" to materialId)
                )
                is Result.Failure -> ActionResult.Error(message = result.error)
                is Result.Loading -> ActionResult.Error(message = "Unexpected loading state")
            }
        } catch (e: Exception) {
            ActionResult.Error(message = e.message ?: "Failed to update material")
        }
    }
}

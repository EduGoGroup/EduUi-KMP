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
 * Handler para crear materiales.
 * Flujo: POST /v1/materials -> navegar a material-detail con el ID creado.
 */
class MaterialCreateHandler(
    private val dataLoader: DataLoader
) : ScreenActionHandler {

    override val screenKeys = setOf("material-create")

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

        val body = JsonObject(buildMap {
            put("title", JsonPrimitive(title))
            fieldValues["subject"]?.let { put("subject", JsonPrimitive(it)) }
            fieldValues["grade"]?.let { put("grade", JsonPrimitive(it)) }
            fieldValues["description"]?.let { put("description", JsonPrimitive(it)) }
        })

        return try {
            when (val result = dataLoader.submitData("/v1/materials", body)) {
                is Result.Success -> {
                    val materialId = result.data?.get("id")?.jsonPrimitive?.content
                    if (materialId != null) {
                        ActionResult.NavigateTo(
                            screenKey = "material-detail",
                            params = mapOf("id" to materialId)
                        )
                    } else {
                        ActionResult.NavigateTo(screenKey = "materials-list")
                    }
                }
                is Result.Failure -> ActionResult.Error(message = result.error)
                is Result.Loading -> ActionResult.Error(message = "Unexpected loading state")
            }
        } catch (e: Exception) {
            ActionResult.Error(message = e.message ?: "Failed to create material")
        }
    }
}

package com.edugo.kmp.dynamicui.handler.handlers

import com.edugo.kmp.dynamicui.action.ActionContext
import com.edugo.kmp.dynamicui.action.ActionResult
import com.edugo.kmp.dynamicui.handler.ScreenActionHandler
import com.edugo.kmp.dynamicui.model.ActionDefinition
import com.edugo.kmp.dynamicui.model.ActionType

/**
 * Handler para pantallas de progreso.
 * Intercepta acciones en progress-my y progress-student-detail.
 * Maneja navegación personalizada entre vistas de progreso.
 */
class ProgressHandler : ScreenActionHandler {

    override val screenKeys = setOf("progress-my", "progress-unit-list", "progress-student-detail")

    override fun canHandle(action: ActionDefinition): Boolean {
        return action.type == ActionType.NAVIGATE || action.type == ActionType.REFRESH
    }

    override suspend fun handle(
        action: ActionDefinition,
        context: ActionContext
    ): ActionResult {
        return when (action.type) {
            ActionType.REFRESH -> ActionResult.Success(message = null)
            else -> {
                // Delegate navigation to generic handler
                ActionResult.Success(message = null)
            }
        }
    }
}

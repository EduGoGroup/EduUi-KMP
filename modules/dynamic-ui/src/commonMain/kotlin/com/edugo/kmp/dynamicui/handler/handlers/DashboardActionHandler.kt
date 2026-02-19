package com.edugo.kmp.dynamicui.handler.handlers

import com.edugo.kmp.dynamicui.action.ActionContext
import com.edugo.kmp.dynamicui.action.ActionResult
import com.edugo.kmp.dynamicui.handler.ScreenActionHandler
import com.edugo.kmp.dynamicui.model.ActionDefinition

/**
 * Handler de prueba para el dashboard.
 * Intercepta todas las acciones de botones y muestra un mensaje "Hola Mundo".
 */
class DashboardActionHandler : ScreenActionHandler {

    override val screenKeys = setOf("dashboard-teacher", "dashboard-student")

    override fun canHandle(action: ActionDefinition): Boolean = true

    override suspend fun handle(
        action: ActionDefinition,
        context: ActionContext
    ): ActionResult {
        return ActionResult.Success(
            message = "Hola Mundo - ${action.id} (type: ${action.type})"
        )
    }
}

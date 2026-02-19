package com.edugo.kmp.dynamicui.handler

import com.edugo.kmp.dynamicui.action.ActionContext
import com.edugo.kmp.dynamicui.action.ActionResult
import com.edugo.kmp.dynamicui.model.ActionDefinition

/**
 * Interface for screen-specific action handlers.
 * Allows screens to override default action behavior with custom logic.
 */
interface ScreenActionHandler {
    /** Screen keys this handler is responsible for */
    val screenKeys: Set<String>

    /** Returns true if this handler can process the given action */
    fun canHandle(action: ActionDefinition): Boolean

    /** Execute the action with custom logic */
    suspend fun handle(
        action: ActionDefinition,
        context: ActionContext
    ): ActionResult
}

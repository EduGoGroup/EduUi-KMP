package com.edugo.kmp.dynamicui.handler

import com.edugo.kmp.dynamicui.model.ActionDefinition

/**
 * Registry that maps screen keys to their custom action handlers.
 * When a screen has a registered handler that can process an action,
 * it takes priority over the generic ActionRegistry handlers.
 */
class ScreenHandlerRegistry(
    handlers: List<ScreenActionHandler> = emptyList()
) {
    private val handlerMap: Map<String, List<ScreenActionHandler>>

    init {
        val map = mutableMapOf<String, MutableList<ScreenActionHandler>>()
        handlers.forEach { handler ->
            handler.screenKeys.forEach { key ->
                map.getOrPut(key) { mutableListOf() }.add(handler)
            }
        }
        handlerMap = map
    }

    /**
     * Find a handler for the given screen key and action.
     * Returns null if no custom handler is registered.
     */
    fun findHandler(screenKey: String, action: ActionDefinition): ScreenActionHandler? {
        return handlerMap[screenKey]?.firstOrNull { it.canHandle(action) }
    }

    /**
     * Check if a custom handler exists for this screen + action combination.
     */
    fun hasHandler(screenKey: String, action: ActionDefinition): Boolean {
        return findHandler(screenKey, action) != null
    }
}

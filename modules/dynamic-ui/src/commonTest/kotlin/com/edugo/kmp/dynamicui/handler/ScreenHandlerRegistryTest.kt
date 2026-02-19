package com.edugo.kmp.dynamicui.handler

import com.edugo.kmp.dynamicui.action.ActionContext
import com.edugo.kmp.dynamicui.action.ActionResult
import com.edugo.kmp.dynamicui.model.ActionDefinition
import com.edugo.kmp.dynamicui.model.ActionTrigger
import com.edugo.kmp.dynamicui.model.ActionType
import kotlinx.serialization.json.JsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ScreenHandlerRegistryTest {

    private fun createAction(type: ActionType, id: String = "test"): ActionDefinition {
        return ActionDefinition(
            id = id,
            trigger = ActionTrigger.BUTTON_CLICK,
            type = type,
            config = JsonObject(emptyMap())
        )
    }

    private fun createHandler(
        keys: Set<String>,
        handleTypes: Set<ActionType>
    ): ScreenActionHandler {
        return object : ScreenActionHandler {
            override val screenKeys = keys
            override fun canHandle(action: ActionDefinition) = action.type in handleTypes
            override suspend fun handle(
                action: ActionDefinition,
                context: ActionContext
            ): ActionResult = ActionResult.Success()
        }
    }

    @Test
    fun findHandler_returns_correct_handler_for_registered_screenKey_and_action() {
        val handler = createHandler(setOf("app-login"), setOf(ActionType.SUBMIT_FORM))
        val registry = ScreenHandlerRegistry(listOf(handler))

        val found = registry.findHandler("app-login", createAction(ActionType.SUBMIT_FORM))
        assertNotNull(found)
        assertEquals(handler, found)
    }

    @Test
    fun findHandler_returns_null_for_unregistered_screenKey() {
        val handler = createHandler(setOf("app-login"), setOf(ActionType.SUBMIT_FORM))
        val registry = ScreenHandlerRegistry(listOf(handler))

        val found = registry.findHandler("unknown-screen", createAction(ActionType.SUBMIT_FORM))
        assertNull(found)
    }

    @Test
    fun findHandler_returns_null_when_handler_cannot_handle_action() {
        val handler = createHandler(setOf("app-login"), setOf(ActionType.SUBMIT_FORM))
        val registry = ScreenHandlerRegistry(listOf(handler))

        val found = registry.findHandler("app-login", createAction(ActionType.NAVIGATE))
        assertNull(found)
    }

    @Test
    fun hasHandler_returns_true_when_handler_exists() {
        val handler = createHandler(setOf("app-login"), setOf(ActionType.SUBMIT_FORM))
        val registry = ScreenHandlerRegistry(listOf(handler))

        assertTrue(registry.hasHandler("app-login", createAction(ActionType.SUBMIT_FORM)))
    }

    @Test
    fun hasHandler_returns_false_when_no_handler() {
        val handler = createHandler(setOf("app-login"), setOf(ActionType.SUBMIT_FORM))
        val registry = ScreenHandlerRegistry(listOf(handler))

        assertFalse(registry.hasHandler("unknown", createAction(ActionType.SUBMIT_FORM)))
    }

    @Test
    fun multiple_handlers_for_same_screenKey_first_match_wins() {
        val handler1 = createHandler(setOf("app-settings"), setOf(ActionType.LOGOUT))
        val handler2 = createHandler(setOf("app-settings"), setOf(ActionType.LOGOUT, ActionType.CONFIRM))
        val registry = ScreenHandlerRegistry(listOf(handler1, handler2))

        val found = registry.findHandler("app-settings", createAction(ActionType.LOGOUT))
        assertEquals(handler1, found)
    }

    @Test
    fun handler_registered_for_multiple_screenKeys() {
        val handler = createHandler(setOf("app-login", "app-login-es"), setOf(ActionType.SUBMIT_FORM))
        val registry = ScreenHandlerRegistry(listOf(handler))

        assertNotNull(registry.findHandler("app-login", createAction(ActionType.SUBMIT_FORM)))
        assertNotNull(registry.findHandler("app-login-es", createAction(ActionType.SUBMIT_FORM)))
    }

    @Test
    fun empty_registry_returns_null() {
        val registry = ScreenHandlerRegistry()

        assertNull(registry.findHandler("app-login", createAction(ActionType.SUBMIT_FORM)))
        assertFalse(registry.hasHandler("app-login", createAction(ActionType.SUBMIT_FORM)))
    }
}

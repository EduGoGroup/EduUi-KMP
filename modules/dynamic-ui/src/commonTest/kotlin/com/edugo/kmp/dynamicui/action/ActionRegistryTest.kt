package com.edugo.kmp.dynamicui.action

import com.edugo.kmp.dynamicui.action.handlers.ApiCallHandler
import com.edugo.kmp.dynamicui.action.handlers.ConfirmHandler
import com.edugo.kmp.dynamicui.action.handlers.LogoutHandler
import com.edugo.kmp.dynamicui.action.handlers.NavigateHandler
import com.edugo.kmp.dynamicui.action.handlers.RefreshHandler
import com.edugo.kmp.dynamicui.action.handlers.SubmitFormHandler
import com.edugo.kmp.dynamicui.model.ActionType
import com.edugo.kmp.network.EduGoHttpClient
import com.edugo.kmp.network.HttpClientFactory
import kotlin.test.Test
import kotlin.test.assertIs

class ActionRegistryTest {

    private fun createRegistry(): ActionRegistry {
        val httpClient = EduGoHttpClient.create()
        return ActionRegistry(
            navigateHandler = NavigateHandler(),
            apiCallHandler = ApiCallHandler(httpClient, "http://localhost"),
            refreshHandler = RefreshHandler(),
            submitFormHandler = SubmitFormHandler(httpClient, "http://localhost"),
            confirmHandler = ConfirmHandler(),
            logoutHandler = LogoutHandler()
        )
    }

    @Test
    fun resolve_returns_NavigateHandler_for_NAVIGATE() {
        val registry = createRegistry()
        val handler = registry.resolve(ActionType.NAVIGATE)
        assertIs<NavigateHandler>(handler)
    }

    @Test
    fun resolve_returns_NavigateHandler_for_NAVIGATE_BACK() {
        val registry = createRegistry()
        val handler = registry.resolve(ActionType.NAVIGATE_BACK)
        assertIs<NavigateHandler>(handler)
    }

    @Test
    fun resolve_returns_ApiCallHandler_for_API_CALL() {
        val registry = createRegistry()
        val handler = registry.resolve(ActionType.API_CALL)
        assertIs<ApiCallHandler>(handler)
    }

    @Test
    fun resolve_returns_SubmitFormHandler_for_SUBMIT_FORM() {
        val registry = createRegistry()
        val handler = registry.resolve(ActionType.SUBMIT_FORM)
        assertIs<SubmitFormHandler>(handler)
    }

    @Test
    fun resolve_returns_RefreshHandler_for_REFRESH() {
        val registry = createRegistry()
        val handler = registry.resolve(ActionType.REFRESH)
        assertIs<RefreshHandler>(handler)
    }

    @Test
    fun resolve_returns_ConfirmHandler_for_CONFIRM() {
        val registry = createRegistry()
        val handler = registry.resolve(ActionType.CONFIRM)
        assertIs<ConfirmHandler>(handler)
    }

    @Test
    fun resolve_returns_LogoutHandler_for_LOGOUT() {
        val registry = createRegistry()
        val handler = registry.resolve(ActionType.LOGOUT)
        assertIs<LogoutHandler>(handler)
    }

    @Test
    fun resolve_covers_all_action_types() {
        val registry = createRegistry()
        ActionType.entries.forEach { actionType ->
            val handler = registry.resolve(actionType)
            assertIs<ActionHandler>(handler)
        }
    }
}

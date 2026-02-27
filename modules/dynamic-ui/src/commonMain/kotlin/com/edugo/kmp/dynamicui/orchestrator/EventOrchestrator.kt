package com.edugo.kmp.dynamicui.orchestrator

import com.edugo.kmp.auth.model.UserContext
import com.edugo.kmp.dynamicui.contract.EventContext
import com.edugo.kmp.dynamicui.contract.EventResult
import com.edugo.kmp.dynamicui.contract.ScreenContractRegistry
import com.edugo.kmp.dynamicui.contract.ScreenEvent
import com.edugo.kmp.dynamicui.data.DataLoader
import com.edugo.kmp.foundation.result.Result
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class EventOrchestrator(
    private val registry: ScreenContractRegistry,
    private val dataLoader: DataLoader,
    private val userContextProvider: () -> UserContext?
) {

    suspend fun execute(
        screenKey: String,
        event: ScreenEvent,
        context: EventContext
    ): EventResult {
        val contract = registry.find(screenKey)
            ?: return EventResult.Error("No contract found for screen: $screenKey")

        // Check permission
        val requiredPermission = contract.permissionFor(event)
        if (requiredPermission != null) {
            val userContext = userContextProvider()
            if (userContext == null || !userContext.hasPermission(requiredPermission)) {
                return EventResult.PermissionDenied
            }
        }

        // Get endpoint - SELECT_ITEM may delegate to a custom handler instead
        val endpoint = contract.endpointFor(event, context)
        if (endpoint == null) {
            if (event == ScreenEvent.SELECT_ITEM) {
                // Fallback: try custom handler "select-item"
                val handler = contract.customEventHandlers()["select-item"]
                if (handler != null) {
                    return try { handler.execute(context) } catch (e: Exception) {
                        EventResult.Error(e.message ?: "Select item failed")
                    }
                }
                return EventResult.NoOp
            }
            if (event == ScreenEvent.CREATE) {
                val handler = contract.customEventHandlers()["create"]
                if (handler != null) {
                    return try { handler.execute(context) } catch (e: Exception) {
                        EventResult.Error(e.message ?: "Create navigation failed")
                    }
                }
                // Default: navigate to {resource}-form
                return EventResult.NavigateTo("${contract.resource}-form")
            }
            return EventResult.Error("No endpoint defined for event: $event on screen: $screenKey")
        }

        // Execute before hook
        val requestConfig = contract.beforeRequest(event, endpoint, context)

        // Determine HTTP method based on event
        val method = resolveMethod(event, requestConfig.method)

        return try {
            when (method) {
                "GET" -> executeGet(contract, event, requestConfig, context)
                "POST" -> executeSubmit(contract, event, requestConfig, context, "POST")
                "PUT" -> executeSubmit(contract, event, requestConfig, context, "PUT")
                "DELETE" -> {
                    val itemId = context.selectedItem?.get("id")?.let {
                        (it as? JsonPrimitive)?.content
                    }
                    if (itemId.isNullOrEmpty()) {
                        EventResult.Error("No se pudo identificar el elemento a eliminar")
                    } else {
                        EventResult.PendingDelete(
                            itemId = itemId,
                            endpoint = requestConfig.url,
                            method = "DELETE"
                        )
                    }
                }
                else -> EventResult.Error("Unsupported method: $method")
            }
        } catch (e: Exception) {
            EventResult.Error(e.message ?: "Unexpected error")
        }
    }

    suspend fun executeCustom(
        screenKey: String,
        eventId: String,
        context: EventContext
    ): EventResult {
        val contract = registry.find(screenKey)
            ?: return EventResult.Error("No contract found for screen: $screenKey")

        val handler = contract.customEventHandlers()[eventId]
            ?: return EventResult.Error("No custom handler for event: $eventId")

        // Check permission
        val requiredPermission = handler.requiredPermission
        if (requiredPermission != null) {
            val userContext = userContextProvider()
            if (userContext != null && !userContext.hasPermission(requiredPermission)) {
                return EventResult.PermissionDenied
            }
        }

        return try {
            handler.execute(context)
        } catch (e: Exception) {
            EventResult.Error(e.message ?: "Custom event execution failed")
        }
    }

    fun canExecute(screenKey: String, event: ScreenEvent): Boolean {
        val contract = registry.find(screenKey) ?: return false
        val requiredPermission = contract.permissionFor(event) ?: return true
        val userContext = userContextProvider() ?: return true
        return userContext.hasPermission(requiredPermission)
    }

    fun canExecuteCustom(screenKey: String, eventId: String): Boolean {
        val contract = registry.find(screenKey) ?: return false
        val handler = contract.customEventHandlers()[eventId] ?: return false
        val requiredPermission = handler.requiredPermission ?: return true
        val userContext = userContextProvider() ?: return true
        return userContext.hasPermission(requiredPermission)
    }

    private suspend fun executeGet(
        contract: com.edugo.kmp.dynamicui.contract.ScreenContract,
        event: ScreenEvent,
        requestConfig: com.edugo.kmp.dynamicui.contract.RequestConfig,
        context: EventContext
    ): EventResult {
        val config = contract.dataConfig()
        val extraParams = requestConfig.params + context.params
        return when (val result = dataLoader.loadData(requestConfig.url, config, extraParams)) {
            is Result.Success -> EventResult.Success(
                data = JsonObject(mapOf(
                    "items" to kotlinx.serialization.json.JsonArray(
                        result.data.items.map { it }
                    ),
                    "hasMore" to JsonPrimitive(result.data.hasMore)
                ))
            )
            is Result.Failure -> EventResult.Error(result.error)
            is Result.Loading -> EventResult.Error("Unexpected loading state")
        }
    }

    private suspend fun executeSubmit(
        contract: com.edugo.kmp.dynamicui.contract.ScreenContract,
        event: ScreenEvent,
        requestConfig: com.edugo.kmp.dynamicui.contract.RequestConfig,
        context: EventContext,
        method: String
    ): EventResult {
        val body = JsonObject(
            context.fieldValues.mapValues { (_, value) -> JsonPrimitive(value) }
        )
        return when (val result = dataLoader.submitData(requestConfig.url, body, method)) {
            is Result.Success -> {
                val responseData = result.data ?: JsonObject(emptyMap())
                val processed = contract.afterResponse(event, responseData)
                EventResult.Success(data = processed)
            }
            is Result.Failure -> EventResult.Error(result.error)
            is Result.Loading -> EventResult.Error("Unexpected loading state")
        }
    }

    private fun resolveMethod(event: ScreenEvent, configMethod: String): String {
        if (configMethod != "GET") return configMethod
        return when (event) {
            ScreenEvent.LOAD_DATA, ScreenEvent.SEARCH,
            ScreenEvent.REFRESH, ScreenEvent.LOAD_MORE -> "GET"
            ScreenEvent.SAVE_NEW -> "POST"
            ScreenEvent.SAVE_EXISTING -> "PUT"
            ScreenEvent.DELETE -> "DELETE"
            ScreenEvent.SELECT_ITEM -> "GET"
            ScreenEvent.CREATE -> "GET"
        }
    }
}

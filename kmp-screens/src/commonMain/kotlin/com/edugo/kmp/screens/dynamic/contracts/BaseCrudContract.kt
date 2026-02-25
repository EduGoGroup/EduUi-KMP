package com.edugo.kmp.screens.dynamic.contracts

import com.edugo.kmp.dynamicui.contract.EventContext
import com.edugo.kmp.dynamicui.contract.ScreenContract
import com.edugo.kmp.dynamicui.contract.ScreenEvent

/**
 * Abstract base class for standard CRUD screens.
 *
 * Provides default endpoint resolution for list, create, update, and delete operations
 * based on a configurable API prefix and base path.
 *
 * @param apiPrefix The API routing prefix ("admin:", "", "iam:")
 * @param basePath The base REST path (e.g., "/api/v1/subjects")
 * @param resource The resource name used for permission checks
 */
abstract class BaseCrudContract(
    private val apiPrefix: String,
    private val basePath: String,
    override val resource: String
) : ScreenContract {

    override fun endpointFor(event: ScreenEvent, context: EventContext): String? = when (event) {
        ScreenEvent.LOAD_DATA, ScreenEvent.SEARCH, ScreenEvent.REFRESH, ScreenEvent.LOAD_MORE ->
            "$apiPrefix$basePath"
        ScreenEvent.SAVE_NEW ->
            "$apiPrefix$basePath"
        ScreenEvent.SAVE_EXISTING -> {
            val id = context.params["id"]
            if (id != null) "$apiPrefix$basePath/$id" else null
        }
        ScreenEvent.DELETE -> {
            val id = context.params["id"]
            if (id != null) "$apiPrefix$basePath/$id" else null
        }
        ScreenEvent.SELECT_ITEM, ScreenEvent.CREATE -> null
    }

    /**
     * Resolves placeholders in an endpoint template using values from the context params.
     * For example, "/api/v1/schools/{schoolId}/units" with params["schoolId"] = "123"
     * becomes "/api/v1/schools/123/units".
     */
    protected fun resolveEndpoint(template: String, context: EventContext): String {
        var resolved = template
        context.params.forEach { (key, value) ->
            resolved = resolved.replace("{$key}", value)
        }
        return resolved
    }
}

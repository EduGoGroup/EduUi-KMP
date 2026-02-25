package com.edugo.kmp.screens.dynamic.contracts

import com.edugo.kmp.dynamicui.contract.EventContext
import com.edugo.kmp.dynamicui.contract.ScreenContract
import com.edugo.kmp.dynamicui.contract.ScreenEvent

/**
 * Abstract base class for dashboard screens.
 *
 * Dashboards only support LOAD_DATA and REFRESH events, returning a fixed data endpoint.
 * They do not support CRUD operations.
 *
 * @param resource The resource name used for permission checks
 * @param dataEndpoint The endpoint to load dashboard data from
 */
abstract class BaseDashboardContract(
    override val resource: String,
    private val dataEndpoint: String
) : ScreenContract {

    override fun endpointFor(event: ScreenEvent, context: EventContext): String? = when (event) {
        ScreenEvent.LOAD_DATA, ScreenEvent.REFRESH -> dataEndpoint
        else -> null
    }
}

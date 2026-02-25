package com.edugo.kmp.screens.dynamic.contracts

import com.edugo.kmp.dynamicui.contract.EventContext
import com.edugo.kmp.dynamicui.contract.ScreenContract
import com.edugo.kmp.dynamicui.contract.ScreenEvent

/**
 * Contract for guardian-related screens: children-list and child-progress.
 *
 * Instantiated separately for each screenKey in DI.
 */
class GuardianContract(
    override val screenKey: String
) : ScreenContract {
    override val resource = "progress"

    override fun endpointFor(event: ScreenEvent, context: EventContext): String? = when (event) {
        ScreenEvent.LOAD_DATA, ScreenEvent.REFRESH -> resolveDataEndpoint(context)
        else -> null
    }

    private fun resolveDataEndpoint(context: EventContext): String = when (screenKey) {
        "dashboard-guardian" -> "/api/v1/guardians/me/stats"
        "children-list" -> "/api/v1/guardians/me/children"
        "child-progress" -> {
            val childId = context.params["childId"]
            if (childId != null) "/api/v1/guardians/me/children/$childId/progress"
            else "/api/v1/guardians/me/stats"
        }
        else -> "/api/v1/guardians/me/stats"
    }
}

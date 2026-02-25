package com.edugo.kmp.screens.dynamic.contracts

import com.edugo.kmp.dynamicui.contract.EventContext
import com.edugo.kmp.dynamicui.contract.ScreenEvent
import com.edugo.kmp.dynamicui.model.DataConfig

class UnitsListContract : BaseCrudContract(
    apiPrefix = "admin:",
    basePath = "/api/v1/schools/{schoolId}/units",
    resource = "academic_units"
) {
    override val screenKey = "units-list"

    override fun dataConfig() = DataConfig(
        fieldMapping = mapOf(
            "title" to "name",
            "subtitle" to "description",
            "status" to "is_active"
        )
    )

    override fun endpointFor(event: ScreenEvent, context: EventContext): String? {
        val schoolId = context.params["schoolId"] ?: return null
        val resolvedBase = "/api/v1/schools/$schoolId/units"

        return when (event) {
            ScreenEvent.LOAD_DATA, ScreenEvent.SEARCH, ScreenEvent.REFRESH, ScreenEvent.LOAD_MORE ->
                "admin:$resolvedBase"
            ScreenEvent.SAVE_NEW ->
                "admin:$resolvedBase"
            ScreenEvent.SAVE_EXISTING -> {
                val id = context.params["id"]
                if (id != null) "admin:$resolvedBase/$id" else null
            }
            ScreenEvent.DELETE -> {
                val id = context.params["id"]
                if (id != null) "admin:$resolvedBase/$id" else null
            }
            ScreenEvent.SELECT_ITEM -> null
        }
    }
}

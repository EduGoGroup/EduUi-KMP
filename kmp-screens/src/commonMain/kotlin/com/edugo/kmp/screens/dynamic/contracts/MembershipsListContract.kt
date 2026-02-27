package com.edugo.kmp.screens.dynamic.contracts

import com.edugo.kmp.dynamicui.contract.EventContext
import com.edugo.kmp.dynamicui.contract.ScreenEvent
import com.edugo.kmp.dynamicui.model.DataConfig
import com.edugo.kmp.dynamicui.model.PaginationConfig

class MembershipsListContract : BaseCrudContract(
    apiPrefix = "admin:",
    basePath = "/api/v1/memberships",
    resource = "memberships"
) {
    override val screenKey = "memberships-list"

    override fun dataConfig() = DataConfig(
        pagination = PaginationConfig(),
        fieldMapping = mapOf(
            "title" to "user_id",
            "subtitle" to "role",
            "status" to "is_active"
        ),
        searchFields = listOf("role")
    )

    override fun endpointFor(event: ScreenEvent, context: EventContext): String? {
        val unitId = context.params["unitId"] ?: return null
        val base = "admin:/api/v1/memberships"

        return when (event) {
            ScreenEvent.LOAD_DATA, ScreenEvent.SEARCH, ScreenEvent.REFRESH, ScreenEvent.LOAD_MORE ->
                "$base?unit_id=$unitId"
            ScreenEvent.SAVE_NEW ->
                base
            ScreenEvent.SAVE_EXISTING -> {
                val id = context.params["id"]
                if (id != null) "$base/$id" else null
            }
            ScreenEvent.DELETE -> {
                val id = context.params["id"]
                if (id != null) "$base/$id" else null
            }
            ScreenEvent.SELECT_ITEM, ScreenEvent.CREATE -> null
        }
    }
}

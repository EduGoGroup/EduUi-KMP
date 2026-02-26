package com.edugo.kmp.screens.dynamic.contracts

import com.edugo.kmp.dynamicui.model.DataConfig
import com.edugo.kmp.dynamicui.model.PaginationConfig

class RolesListContract : BaseCrudContract(
    apiPrefix = "iam:",
    basePath = "/api/v1/roles",
    resource = "roles"
) {
    override val screenKey = "roles-list"

    override fun dataConfig() = DataConfig(
        pagination = PaginationConfig(),
        fieldMapping = mapOf(
            "title" to "display_name",
            "subtitle" to "description",
            "status" to "is_active"
        ),
        searchFields = listOf("display_name", "name")
    )
}

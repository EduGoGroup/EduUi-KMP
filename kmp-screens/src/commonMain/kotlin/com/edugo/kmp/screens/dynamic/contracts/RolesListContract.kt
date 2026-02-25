package com.edugo.kmp.screens.dynamic.contracts

import com.edugo.kmp.dynamicui.model.DataConfig

class RolesListContract : BaseCrudContract(
    apiPrefix = "iam:",
    basePath = "/api/v1/roles",
    resource = "roles"
) {
    override val screenKey = "roles-list"

    override fun dataConfig() = DataConfig(
        fieldMapping = mapOf(
            "title" to "display_name",
            "subtitle" to "description",
            "status" to "is_active"
        )
    )
}

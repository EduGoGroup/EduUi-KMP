package com.edugo.kmp.screens.dynamic.contracts

import com.edugo.kmp.dynamicui.model.DataConfig

class PermissionsListContract : BaseCrudContract(
    apiPrefix = "iam:",
    basePath = "/api/v1/permissions",
    resource = "permissions_mgmt"
) {
    override val screenKey = "permissions-list"

    override fun dataConfig() = DataConfig(
        fieldMapping = mapOf(
            "title" to "display_name",
            "subtitle" to "description",
            "status" to "is_active"
        )
    )
}

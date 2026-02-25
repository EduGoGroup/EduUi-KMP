package com.edugo.kmp.screens.dynamic.contracts

import com.edugo.kmp.dynamicui.model.DataConfig

class MembershipsListContract : BaseCrudContract(
    apiPrefix = "admin:",
    basePath = "/api/v1/memberships",
    resource = "memberships"
) {
    override val screenKey = "memberships-list"

    override fun dataConfig() = DataConfig(
        fieldMapping = mapOf(
            "title" to "full_name",
            "subtitle" to "role_name",
            "status" to "is_active"
        )
    )
}

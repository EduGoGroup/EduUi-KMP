package com.edugo.kmp.screens.dynamic.contracts

import com.edugo.kmp.dynamicui.model.DataConfig

class MaterialsListContract : BaseCrudContract(
    apiPrefix = "",
    basePath = "/api/v1/materials",
    resource = "materials"
) {
    override val screenKey = "materials-list"

    override fun dataConfig() = DataConfig(
        fieldMapping = mapOf(
            "title" to "name",
            "subtitle" to "description",
            "status" to "is_active"
        )
    )
}

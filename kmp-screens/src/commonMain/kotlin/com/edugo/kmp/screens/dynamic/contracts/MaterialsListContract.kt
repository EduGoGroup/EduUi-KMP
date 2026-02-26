package com.edugo.kmp.screens.dynamic.contracts

import com.edugo.kmp.dynamicui.model.DataConfig
import com.edugo.kmp.dynamicui.model.PaginationConfig

class MaterialsListContract : BaseCrudContract(
    apiPrefix = "",
    basePath = "/api/v1/materials",
    resource = "materials"
) {
    override val screenKey = "materials-list"

    override fun dataConfig() = DataConfig(
        pagination = PaginationConfig(),
        fieldMapping = mapOf(
            "title" to "name",
            "subtitle" to "description",
            "status" to "is_active"
        ),
        searchFields = listOf("name")
    )
}

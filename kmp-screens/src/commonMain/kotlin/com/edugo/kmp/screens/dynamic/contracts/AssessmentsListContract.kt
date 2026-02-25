package com.edugo.kmp.screens.dynamic.contracts

import com.edugo.kmp.dynamicui.model.DataConfig

class AssessmentsListContract : BaseCrudContract(
    apiPrefix = "",
    basePath = "/api/v1/assessments",
    resource = "assessments"
) {
    override val screenKey = "assessments-list"

    override fun dataConfig() = DataConfig(
        fieldMapping = mapOf(
            "title" to "name",
            "subtitle" to "description",
            "status" to "is_active"
        )
    )
}

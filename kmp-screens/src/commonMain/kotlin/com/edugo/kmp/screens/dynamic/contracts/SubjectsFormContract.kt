package com.edugo.kmp.screens.dynamic.contracts

class SubjectsFormContract : BaseCrudContract(
    apiPrefix = "admin:",
    basePath = "/api/v1/subjects",
    resource = "subjects"
) {
    override val screenKey = "subjects-form"
}

package com.edugo.kmp.screens.dynamic.contracts

class DashboardStudentContract : BaseDashboardContract(
    resource = "stats",
    dataEndpoint = "/api/v1/stats/student"
) {
    override val screenKey = "dashboard-student"
}

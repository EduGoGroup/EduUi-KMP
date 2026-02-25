package com.edugo.kmp.screens.dynamic.contracts

class DashboardTeacherContract : BaseDashboardContract(
    resource = "stats",
    dataEndpoint = "/api/v1/stats/global"
) {
    override val screenKey = "dashboard-teacher"
}

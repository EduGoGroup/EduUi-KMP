package com.edugo.kmp.screens.dynamic.contracts

class ProgressDashboardContract : BaseDashboardContract(
    resource = "progress",
    dataEndpoint = "/api/v1/stats/progress"
) {
    override val screenKey = "progress-dashboard"
}

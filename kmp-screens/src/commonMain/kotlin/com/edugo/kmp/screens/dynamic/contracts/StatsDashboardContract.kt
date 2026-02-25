package com.edugo.kmp.screens.dynamic.contracts

class StatsDashboardContract : BaseDashboardContract(
    resource = "stats",
    dataEndpoint = "admin:/api/v1/stats/global"
) {
    override val screenKey = "stats-dashboard"
}

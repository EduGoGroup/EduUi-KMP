package com.edugo.kmp.screens.dynamic.contracts

class DashboardSchooladminContract : BaseDashboardContract(
    resource = "stats",
    dataEndpoint = "/api/v1/stats/global"
) {
    override val screenKey = "dashboard-schooladmin"
}

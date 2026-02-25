package com.edugo.kmp.screens.dynamic.contracts

class DashboardSuperadminContract : BaseDashboardContract(
    resource = "stats",
    dataEndpoint = "admin:/api/v1/stats/global"
) {
    override val screenKey = "dashboard-superadmin"
}

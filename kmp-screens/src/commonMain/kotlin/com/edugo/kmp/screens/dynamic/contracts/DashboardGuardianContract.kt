package com.edugo.kmp.screens.dynamic.contracts

class DashboardGuardianContract : BaseDashboardContract(
    resource = "progress",
    dataEndpoint = "/api/v1/guardians/me/stats"
) {
    override val screenKey = "dashboard-guardian"
}

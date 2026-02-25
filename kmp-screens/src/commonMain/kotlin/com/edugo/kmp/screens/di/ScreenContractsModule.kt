package com.edugo.kmp.screens.di

import com.edugo.kmp.dynamicui.contract.ScreenContract
import com.edugo.kmp.screens.dynamic.contracts.*
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val screenContractsModule = module {
    // Login & Settings (need AuthService)
    single { LoginContract(get()) } bind ScreenContract::class
    single { SettingsContract(get()) } bind ScreenContract::class

    // Dashboard contracts
    single { DashboardTeacherContract() } bind ScreenContract::class
    single { DashboardStudentContract() } bind ScreenContract::class
    single { DashboardSuperadminContract() } bind ScreenContract::class
    single { DashboardSchooladminContract() } bind ScreenContract::class
    single { DashboardGuardianContract() } bind ScreenContract::class
    single { ProgressDashboardContract() } bind ScreenContract::class
    single { StatsDashboardContract() } bind ScreenContract::class

    // Simple list contracts
    single { SubjectsListContract() } bind ScreenContract::class
    single { SubjectsFormContract() } bind ScreenContract::class
    single { RolesListContract() } bind ScreenContract::class
    single { PermissionsListContract() } bind ScreenContract::class
    single { MembershipsListContract() } bind ScreenContract::class
    single { AssessmentsListContract() } bind ScreenContract::class
    single { MaterialsListContract() } bind ScreenContract::class

    // List contracts with navigation
    single { UsersListContract() } bind ScreenContract::class
    single { SchoolsListContract() } bind ScreenContract::class
    single { SchoolsFormContract() } bind ScreenContract::class
    single { UnitsListContract() } bind ScreenContract::class

    // CRUD contracts (need DataLoader) - use named qualifiers for same type with different screenKey
    single(named("user-create")) { UserCrudContract("user-create", get()) } bind ScreenContract::class
    single(named("user-edit")) { UserCrudContract("user-edit", get()) } bind ScreenContract::class
    single(named("school-create")) { SchoolCrudContract("school-create", get()) } bind ScreenContract::class
    single(named("school-edit")) { SchoolCrudContract("school-edit", get()) } bind ScreenContract::class
    single(named("unit-create")) { UnitCrudContract("unit-create", get()) } bind ScreenContract::class
    single(named("unit-edit")) { UnitCrudContract("unit-edit", get()) } bind ScreenContract::class
    single { MembershipAddContract(get()) } bind ScreenContract::class
    single { MaterialCreateContract(get()) } bind ScreenContract::class
    single { MaterialEditContract(get()) } bind ScreenContract::class
    single { MaterialDetailContract() } bind ScreenContract::class

    // Other contracts
    single { AssessmentTakeContract() } bind ScreenContract::class
    single(named("children-list")) { GuardianContract("children-list") } bind ScreenContract::class
    single(named("child-progress")) { GuardianContract("child-progress") } bind ScreenContract::class
}

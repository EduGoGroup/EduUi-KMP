package com.edugo.kmp.di.module

import com.edugo.kmp.dynamicui.handler.ScreenActionHandler
import com.edugo.kmp.dynamicui.handler.handlers.AssessmentTakeHandler
import com.edugo.kmp.dynamicui.handler.handlers.DashboardActionHandler
import com.edugo.kmp.dynamicui.handler.handlers.LoginActionHandler
import com.edugo.kmp.dynamicui.handler.handlers.MaterialCreateHandler
import com.edugo.kmp.dynamicui.handler.handlers.MaterialEditHandler
import com.edugo.kmp.dynamicui.handler.handlers.GuardianHandler
import com.edugo.kmp.dynamicui.handler.handlers.MembershipHandler
import com.edugo.kmp.dynamicui.handler.handlers.ProgressHandler
import com.edugo.kmp.dynamicui.handler.handlers.SchoolCrudHandler
import com.edugo.kmp.dynamicui.handler.handlers.SettingsActionHandler
import com.edugo.kmp.dynamicui.handler.handlers.UnitCrudHandler
import com.edugo.kmp.dynamicui.handler.handlers.UserCrudHandler
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Registro de ScreenActionHandlers.
 * Agregar una linea por cada handler nuevo.
 */
val screenHandlersModule = module {
    single { LoginActionHandler(get()) } bind ScreenActionHandler::class
    single { SettingsActionHandler(get()) } bind ScreenActionHandler::class
    single { DashboardActionHandler() } bind ScreenActionHandler::class
    single { MaterialCreateHandler(get()) } bind ScreenActionHandler::class
    single { MaterialEditHandler(get()) } bind ScreenActionHandler::class
    single { AssessmentTakeHandler() } bind ScreenActionHandler::class
    single { ProgressHandler() } bind ScreenActionHandler::class
    single { UserCrudHandler(get()) } bind ScreenActionHandler::class
    single { SchoolCrudHandler(get()) } bind ScreenActionHandler::class
    single { UnitCrudHandler(get()) } bind ScreenActionHandler::class
    single { MembershipHandler(get()) } bind ScreenActionHandler::class
    single { GuardianHandler() } bind ScreenActionHandler::class
}

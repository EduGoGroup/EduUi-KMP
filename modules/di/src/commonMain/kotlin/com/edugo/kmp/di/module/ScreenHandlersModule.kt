package com.edugo.kmp.di.module

import com.edugo.kmp.dynamicui.handler.ScreenActionHandler
import com.edugo.kmp.dynamicui.handler.handlers.DashboardActionHandler
import com.edugo.kmp.dynamicui.handler.handlers.LoginActionHandler
import com.edugo.kmp.dynamicui.handler.handlers.SettingsActionHandler
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
}

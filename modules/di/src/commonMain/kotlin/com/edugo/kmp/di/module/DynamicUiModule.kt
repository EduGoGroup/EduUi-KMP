package com.edugo.kmp.di.module

import com.edugo.kmp.config.AppConfig
import com.edugo.kmp.dynamicui.action.ActionRegistry
import com.edugo.kmp.dynamicui.action.handlers.ApiCallHandler
import com.edugo.kmp.dynamicui.action.handlers.ConfirmHandler
import com.edugo.kmp.dynamicui.action.handlers.LogoutHandler
import com.edugo.kmp.dynamicui.action.handlers.NavigateHandler
import com.edugo.kmp.dynamicui.action.handlers.RefreshHandler
import com.edugo.kmp.dynamicui.action.handlers.SubmitFormHandler
import com.edugo.kmp.dynamicui.data.DataLoader
import com.edugo.kmp.dynamicui.data.RemoteDataLoader
import com.edugo.kmp.dynamicui.handler.ScreenHandlerRegistry
import com.edugo.kmp.dynamicui.loader.CachedScreenLoader
import com.edugo.kmp.dynamicui.loader.RemoteScreenLoader
import com.edugo.kmp.dynamicui.loader.ScreenLoader
import com.edugo.kmp.dynamicui.viewmodel.DynamicScreenViewModel
import com.edugo.kmp.network.EduGoHttpClient
import com.edugo.kmp.storage.SafeEduGoStorage
import org.koin.dsl.module

val dynamicUiModule = module {
    single<ScreenLoader> {
        val appConfig = get<AppConfig>()
        // screen endpoints en API Mobile (alineado con frontend Apple)
        CachedScreenLoader(
            remote = RemoteScreenLoader(get<EduGoHttpClient>(), appConfig.mobileApiBaseUrl),
            storage = get<SafeEduGoStorage>()
        )
    }
    single<DataLoader> {
        val appConfig = get<AppConfig>()
        RemoteDataLoader(get<EduGoHttpClient>(), appConfig.mobileApiBaseUrl, appConfig.adminApiBaseUrl)
    }
    single { NavigateHandler() }
    single {
        val appConfig = get<AppConfig>()
        // TODO: ApiCallHandler necesitará enrutamiento inteligente para decidir entre adminApi y mobileApi
        ApiCallHandler(get<EduGoHttpClient>(), appConfig.adminApiBaseUrl)
    }
    single { RefreshHandler() }
    single {
        val appConfig = get<AppConfig>()
        // TODO: SubmitFormHandler necesitará enrutamiento inteligente para decidir entre adminApi y mobileApi
        SubmitFormHandler(get<EduGoHttpClient>(), appConfig.adminApiBaseUrl)
    }
    single { ConfirmHandler() }
    single { LogoutHandler() }
    single { ActionRegistry(get(), get(), get(), get(), get(), get()) }

    // Registry auto-descubre todos los ScreenActionHandler de screenHandlersModule
    single { ScreenHandlerRegistry(getAll()) }

    // ViewModel with handler registry
    factory { DynamicScreenViewModel(get(), get(), get(), get()) }
}

package com.edugo.kmp.di.module

import com.edugo.kmp.config.AppConfig
import com.edugo.kmp.dynamicui.action.ActionRegistry
import com.edugo.kmp.dynamicui.action.handlers.ApiCallHandler
import com.edugo.kmp.dynamicui.action.handlers.ConfirmHandler
import com.edugo.kmp.dynamicui.action.handlers.NavigateHandler
import com.edugo.kmp.dynamicui.action.handlers.RefreshHandler
import com.edugo.kmp.dynamicui.action.handlers.SubmitFormHandler
import com.edugo.kmp.dynamicui.data.DataLoader
import com.edugo.kmp.dynamicui.data.RemoteDataLoader
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
        CachedScreenLoader(
            remote = RemoteScreenLoader(get<EduGoHttpClient>(), appConfig.getFullApiUrl()),
            storage = get<SafeEduGoStorage>()
        )
    }
    single<DataLoader> {
        val appConfig = get<AppConfig>()
        RemoteDataLoader(get<EduGoHttpClient>(), appConfig.getFullApiUrl())
    }
    single { NavigateHandler() }
    single {
        val appConfig = get<AppConfig>()
        ApiCallHandler(get<EduGoHttpClient>(), appConfig.getFullApiUrl())
    }
    single { RefreshHandler() }
    single {
        val appConfig = get<AppConfig>()
        SubmitFormHandler(get<EduGoHttpClient>(), appConfig.getFullApiUrl())
    }
    single { ConfirmHandler() }
    single { ActionRegistry(get(), get(), get(), get(), get()) }
    factory { DynamicScreenViewModel(get(), get(), get()) }
}

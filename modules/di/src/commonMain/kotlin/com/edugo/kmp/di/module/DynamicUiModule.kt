package com.edugo.kmp.di.module

import com.edugo.kmp.auth.service.AuthService
import com.edugo.kmp.auth.service.activeContext
import com.edugo.kmp.config.AppConfig
import com.edugo.kmp.dynamicui.contract.ScreenContract
import com.edugo.kmp.dynamicui.contract.ScreenContractRegistry
import com.edugo.kmp.dynamicui.data.DataLoader
import com.edugo.kmp.dynamicui.data.RemoteDataLoader
import com.edugo.kmp.dynamicui.loader.CachedScreenLoader
import com.edugo.kmp.dynamicui.loader.RemoteScreenLoader
import com.edugo.kmp.dynamicui.loader.ScreenLoader
import com.edugo.kmp.dynamicui.orchestrator.EventOrchestrator
import com.edugo.kmp.dynamicui.viewmodel.DynamicScreenViewModel
import com.edugo.kmp.network.EduGoHttpClient
import com.edugo.kmp.storage.SafeEduGoStorage
import org.koin.dsl.module

val dynamicUiModule = module {
    single<ScreenLoader> {
        val appConfig = get<AppConfig>()
        CachedScreenLoader(
            remote = RemoteScreenLoader(get<EduGoHttpClient>(), appConfig.iamApiBaseUrl),
            storage = get<SafeEduGoStorage>()
        )
    }
    single<DataLoader> {
        val appConfig = get<AppConfig>()
        RemoteDataLoader(get<EduGoHttpClient>(), appConfig.mobileApiBaseUrl, appConfig.adminApiBaseUrl, appConfig.iamApiBaseUrl)
    }
    single { ScreenContractRegistry(getAll()) }
    single {
        val authService = get<AuthService>()
        EventOrchestrator(
            registry = get(),
            dataLoader = get(),
            userContextProvider = { authService.authState.value.activeContext }
        )
    }
    factory { DynamicScreenViewModel(get(), get(), get(), get()) }
}

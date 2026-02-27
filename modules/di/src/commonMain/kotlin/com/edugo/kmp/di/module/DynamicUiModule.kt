package com.edugo.kmp.di.module

import com.edugo.kmp.auth.service.AuthService
import com.edugo.kmp.auth.service.activeContext
import com.edugo.kmp.config.AppConfig
import com.edugo.kmp.dynamicui.cache.CacheConfig
import com.edugo.kmp.dynamicui.cache.RecentScreenTracker
import com.edugo.kmp.dynamicui.contract.ScreenContractRegistry
import com.edugo.kmp.auth.service.AuthState
import com.edugo.kmp.dynamicui.data.CachedDataLoader
import com.edugo.kmp.dynamicui.data.DataLoader
import com.edugo.kmp.dynamicui.data.RemoteDataLoader
import com.edugo.kmp.dynamicui.loader.CachedScreenLoader
import com.edugo.kmp.dynamicui.loader.RemoteScreenLoader
import com.edugo.kmp.dynamicui.loader.ScreenLoader
import com.edugo.kmp.dynamicui.offline.ConflictResolver
import com.edugo.kmp.dynamicui.offline.ConnectivitySyncManager
import com.edugo.kmp.dynamicui.offline.MutationQueue
import com.edugo.kmp.dynamicui.offline.SyncEngine
import com.edugo.kmp.dynamicui.orchestrator.EventOrchestrator
import com.edugo.kmp.dynamicui.sync.DataSyncService
import com.edugo.kmp.dynamicui.sync.LocalSyncStore
import com.edugo.kmp.dynamicui.sync.SyncRepository
import com.edugo.kmp.dynamicui.sync.SyncRepositoryImpl
import com.edugo.kmp.dynamicui.event.ScreenEventBus
import com.edugo.kmp.dynamicui.viewmodel.DynamicScreenViewModel
import com.edugo.kmp.logger.Logger
import com.edugo.kmp.network.EduGoHttpClient
import com.edugo.kmp.network.connectivity.NetworkObserver
import com.edugo.kmp.storage.SafeEduGoStorage
import org.koin.dsl.module

val dynamicUiModule = module {
    // Cache configuration
    single { CacheConfig() }
    single { RecentScreenTracker() }

    // Offline engine
    single { MutationQueue(get<SafeEduGoStorage>()) }
    single { ConflictResolver() }

    single<ScreenLoader> {
        val appConfig = get<AppConfig>()
        CachedScreenLoader(
            remote = RemoteScreenLoader(get<EduGoHttpClient>(), appConfig.iamApiBaseUrl),
            storage = get<SafeEduGoStorage>(),
            cacheConfig = get(),
            networkObserver = getOrNull<NetworkObserver>(),
            logger = getOrNull<Logger>(),
        )
    }
    single<DataLoader> {
        val appConfig = get<AppConfig>()
        val remote = RemoteDataLoader(get<EduGoHttpClient>(), appConfig.mobileApiBaseUrl, appConfig.adminApiBaseUrl, appConfig.iamApiBaseUrl)
        CachedDataLoader(
            remote = remote,
            storage = get<SafeEduGoStorage>(),
            contextKeyProvider = {
                val authService = get<AuthService>()
                val state = authService.authState.value
                (state as? AuthState.Authenticated)?.activeContext?.schoolId ?: ""
            },
            cacheConfig = get(),
            networkObserver = getOrNull<NetworkObserver>(),
            mutationQueue = get(),
            logger = getOrNull<Logger>(),
        )
    }

    // SyncEngine needs a RemoteDataLoader for actual HTTP calls (not the cached wrapper)
    single {
        val appConfig = get<AppConfig>()
        val remote = RemoteDataLoader(get<EduGoHttpClient>(), appConfig.mobileApiBaseUrl, appConfig.adminApiBaseUrl, appConfig.iamApiBaseUrl)
        SyncEngine(get(), remote, get(), get())
    }

    single {
        ConnectivitySyncManager(
            networkObserver = get(),
            syncEngine = get(),
            recentScreenTracker = get(),
            screenLoader = get<ScreenLoader>() as CachedScreenLoader,
        )
    }

    // Sync bundle
    single { LocalSyncStore(get<SafeEduGoStorage>()) }
    single<SyncRepository> {
        val appConfig = get<AppConfig>()
        SyncRepositoryImpl(get<EduGoHttpClient>(), appConfig.iamApiBaseUrl)
    }
    single {
        DataSyncService(
            repository = get(),
            store = get(),
            cachedScreenLoader = get<ScreenLoader>() as CachedScreenLoader,
        )
    }

    single { ScreenEventBus() }
    single { ScreenContractRegistry(getAll()) }
    single {
        val authService = get<AuthService>()
        EventOrchestrator(
            registry = get(),
            dataLoader = get(),
            userContextProvider = { authService.authState.value.activeContext },
        )
    }
    factory {
        DynamicScreenViewModel(
            screenLoader = get(),
            dataLoader = get(),
            orchestrator = get(),
            contractRegistry = get(),
            networkObserver = getOrNull<NetworkObserver>(),
            recentScreenTracker = get(),
            mutationQueue = get(),
            screenEventBus = get(),
        )
    }
}

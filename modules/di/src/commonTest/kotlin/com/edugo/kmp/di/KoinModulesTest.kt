package com.edugo.kmp.di

import com.edugo.kmp.di.module.foundationModule
import com.edugo.kmp.di.module.loggerModule
import com.edugo.kmp.di.module.storageModule
import com.edugo.kmp.foundation.serialization.JsonConfig
import com.edugo.kmp.logger.DefaultLogger
import com.edugo.kmp.logger.Logger
import com.edugo.kmp.storage.AsyncEduGoStorage
import com.edugo.kmp.storage.EduGoStorage
import com.edugo.kmp.storage.SafeEduGoStorage
import com.russhwolf.settings.MapSettings
import kotlinx.serialization.json.Json
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class KoinModulesTest : KoinTest {

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    // ==================== Foundation Module ====================

    @Test
    fun foundationModuleProvidesDefaultJson() {
        startKoin { modules(foundationModule) }

        val json: Json = get()
        assertNotNull(json)
    }

    @Test
    fun foundationModuleProvidesPrettyJson() {
        startKoin { modules(foundationModule) }

        val json: Json = get(named("pretty"))
        assertNotNull(json)
    }

    @Test
    fun foundationModuleProvidesStrictJson() {
        startKoin { modules(foundationModule) }

        val json: Json = get(named("strict"))
        assertNotNull(json)
    }

    @Test
    fun foundationModuleProvidesLenientJson() {
        startKoin { modules(foundationModule) }

        val json: Json = get(named("lenient"))
        assertNotNull(json)
    }

    @Test
    fun foundationJsonDefaultMatchesJsonConfig() {
        startKoin { modules(foundationModule) }

        val json: Json = get()
        assertSame(JsonConfig.Default, json)
    }

    // ==================== Logger Module ====================

    @Test
    fun loggerModuleProvidesLogger() {
        startKoin { modules(loggerModule) }

        val logger: Logger = get()
        assertNotNull(logger)
    }

    @Test
    fun loggerModuleProvidesSingletonLogger() {
        startKoin { modules(loggerModule) }

        val logger1: Logger = get()
        val logger2: Logger = get()
        assertSame(logger1, logger2)
    }

    @Test
    fun loggerModuleProvidesDefaultLogger() {
        startKoin { modules(loggerModule) }

        val logger: Logger = get()
        assertSame(DefaultLogger, logger)
    }

    // ==================== Storage Module (with MapSettings override) ====================

    @Test
    fun storageModuleProvidesEduGoStorage() {
        startKoin {
            modules(
                module { single { EduGoStorage.withSettings(MapSettings()) } },
                module {
                    single { SafeEduGoStorage.wrap(get<EduGoStorage>()) }
                    single { AsyncEduGoStorage(get<EduGoStorage>()) }
                }
            )
        }

        val storage: EduGoStorage = get()
        assertNotNull(storage)
    }

    @Test
    fun storageModuleProvidesSafeEduGoStorage() {
        startKoin {
            modules(
                module { single { EduGoStorage.withSettings(MapSettings()) } },
                module {
                    single { SafeEduGoStorage.wrap(get<EduGoStorage>()) }
                    single { AsyncEduGoStorage(get<EduGoStorage>()) }
                }
            )
        }

        val safeStorage: SafeEduGoStorage = get()
        assertNotNull(safeStorage)
    }

    @Test
    fun storageModuleProvidesAsyncEduGoStorage() {
        startKoin {
            modules(
                module { single { EduGoStorage.withSettings(MapSettings()) } },
                module {
                    single { SafeEduGoStorage.wrap(get<EduGoStorage>()) }
                    single { AsyncEduGoStorage(get<EduGoStorage>()) }
                }
            )
        }

        val asyncStorage: AsyncEduGoStorage = get()
        assertNotNull(asyncStorage)
    }

    @Test
    fun storageModuleSingletonConsistency() {
        startKoin {
            modules(
                module { single { EduGoStorage.withSettings(MapSettings()) } },
                module {
                    single { SafeEduGoStorage.wrap(get<EduGoStorage>()) }
                    single { AsyncEduGoStorage(get<EduGoStorage>()) }
                }
            )
        }

        val storage1: EduGoStorage = get()
        val storage2: EduGoStorage = get()
        assertSame(storage1, storage2)
    }

    // ==================== Core Modules Combination ====================

    @Test
    fun coreModulesLoadWithoutConflict() {
        startKoin {
            modules(KoinInitializer.coreModules())
        }

        val json: Json = get()
        val logger: Logger = get()
        assertNotNull(json)
        assertNotNull(logger)
    }

    @Test
    fun coreModulesReturnCorrectCount() {
        val modules = KoinInitializer.coreModules()
        assertEquals(2, modules.size)
    }

    @Test
    fun allModulesReturnCorrectCount() {
        val modules = KoinInitializer.allModules()
        // core(2) + infrastructure(3) + domain(1) = 6
        assertEquals(6, modules.size)
    }

    @Test
    fun infrastructureModulesReturnCorrectCount() {
        val modules = KoinInitializer.infrastructureModules()
        assertEquals(3, modules.size)
    }

    @Test
    fun domainModulesReturnCorrectCount() {
        val modules = KoinInitializer.domainModules()
        assertEquals(1, modules.size)
    }

    // ==================== Module Override Pattern ====================

    @Test
    fun moduleOverrideWorksCorrectly() {
        startKoin {
            modules(
                foundationModule,
                module {
                    single<Json> { Json { prettyPrint = true } }
                }
            )
        }

        val json: Json = get()
        assertNotNull(json)
    }

    // ==================== Storage Read/Write Integration ====================

    @Test
    fun storageIntegrationWithMapSettings() {
        startKoin {
            modules(
                module { single { EduGoStorage.withSettings(MapSettings()) } },
                module {
                    single { SafeEduGoStorage.wrap(get<EduGoStorage>()) }
                }
            )
        }

        val storage: EduGoStorage = get()
        storage.putString("test_key", "test_value")
        val value = storage.getString("test_key")
        assertEquals("test_value", value)
    }

    @Test
    fun safeStorageIntegrationWithMapSettings() {
        startKoin {
            modules(
                module { single { EduGoStorage.withSettings(MapSettings()) } },
                module {
                    single { SafeEduGoStorage.wrap(get<EduGoStorage>()) }
                }
            )
        }

        val safeStorage: SafeEduGoStorage = get()
        safeStorage.putStringSafe("test_key", "test_value")
        val value = safeStorage.getStringSafe("test_key", "default")
        assertEquals("test_value", value)
    }
}

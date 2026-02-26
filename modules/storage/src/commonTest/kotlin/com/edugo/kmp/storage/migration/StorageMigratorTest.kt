package com.edugo.kmp.storage.migration

import com.edugo.kmp.storage.EduGoStorage
import com.edugo.kmp.storage.SafeEduGoStorage
import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals

class StorageMigratorTest {

    private fun createStorage(): SafeEduGoStorage {
        return SafeEduGoStorage.wrap(EduGoStorage.withSettings(MapSettings()), validateKeys = false)
    }

    /** Helper to build simple migrations that record their execution. */
    private fun migration(ver: Int, block: (SafeEduGoStorage) -> Unit = {}): StorageMigration {
        return object : StorageMigration {
            override val version: Int = ver
            override fun migrate(storage: SafeEduGoStorage) = block(storage)
        }
    }

    @Test
    fun freshInstallRunsAllMigrations() {
        val storage = createStorage()
        val executed = mutableListOf<Int>()

        val migrator = StorageMigrator(
            storage = storage,
            migrations = listOf(
                migration(1) { executed.add(1) },
                migration(2) { executed.add(2) },
                migration(3) { executed.add(3) }
            )
        )

        val result = migrator.migrate()

        assertEquals(3, result)
        assertEquals(listOf(1, 2, 3), executed)
        assertEquals(3, migrator.currentVersion())
    }

    @Test
    fun partialMigrationSkipsAlreadyApplied() {
        val storage = createStorage()
        // Simulate being at version 1
        storage.putIntSafe(StorageMigrator.VERSION_KEY, 1)

        val executed = mutableListOf<Int>()

        val migrator = StorageMigrator(
            storage = storage,
            migrations = listOf(
                migration(1) { executed.add(1) },
                migration(2) { executed.add(2) },
                migration(3) { executed.add(3) }
            )
        )

        val result = migrator.migrate()

        assertEquals(3, result)
        assertEquals(listOf(2, 3), executed)
    }

    @Test
    fun noOpWhenAlreadyAtLatestVersion() {
        val storage = createStorage()
        storage.putIntSafe(StorageMigrator.VERSION_KEY, 3)

        val executed = mutableListOf<Int>()

        val migrator = StorageMigrator(
            storage = storage,
            migrations = listOf(
                migration(1) { executed.add(1) },
                migration(2) { executed.add(2) },
                migration(3) { executed.add(3) }
            )
        )

        val result = migrator.migrate()

        assertEquals(3, result)
        assertEquals(emptyList(), executed)
    }

    @Test
    fun migrationsExecuteInVersionOrder() {
        val storage = createStorage()
        val executed = mutableListOf<Int>()

        // Provide migrations out of order
        val migrator = StorageMigrator(
            storage = storage,
            migrations = listOf(
                migration(3) { executed.add(3) },
                migration(1) { executed.add(1) },
                migration(2) { executed.add(2) }
            )
        )

        migrator.migrate()

        assertEquals(listOf(1, 2, 3), executed)
    }

    @Test
    fun migrationCanModifyStorageData() {
        val storage = createStorage()
        storage.putStringSafe("old.key", "value")

        val migrator = StorageMigrator(
            storage = storage,
            migrations = listOf(
                migration(1) { s ->
                    val old = s.getStringSafe("old.key")
                    s.putStringSafe("new.key", old)
                    s.removeSafe("old.key")
                }
            )
        )

        migrator.migrate()

        assertEquals("value", storage.getStringSafe("new.key"))
        assertEquals("", storage.getStringSafe("old.key"))
    }

    @Test
    fun currentVersionReturnsZeroOnFreshStorage() {
        val storage = createStorage()
        val migrator = StorageMigrator(storage = storage, migrations = emptyList())
        assertEquals(0, migrator.currentVersion())
    }

    @Test
    fun emptyMigrationListIsNoOp() {
        val storage = createStorage()
        val migrator = StorageMigrator(storage = storage, migrations = emptyList())

        val result = migrator.migrate()

        assertEquals(0, result)
    }
}

package com.edugo.kmp.storage.migration

import com.edugo.kmp.logger.DefaultLogger
import com.edugo.kmp.logger.TaggedLogger
import com.edugo.kmp.logger.withTag
import com.edugo.kmp.storage.SafeEduGoStorage

/**
 * Runs pending [StorageMigration]s in order against a [SafeEduGoStorage].
 *
 * The current schema version is persisted under [VERSION_KEY]. On each call
 * to [migrate], only migrations with a version greater than the stored
 * version are executed sequentially. After each successful step the stored
 * version is bumped so a crash mid-migration resumes from the correct point.
 *
 * @property storage The storage instance to migrate
 * @property migrations All known migrations (order is enforced internally)
 * @property logger Logger for progress output
 */
public class StorageMigrator(
    private val storage: SafeEduGoStorage,
    private val migrations: List<StorageMigration>,
    private val logger: TaggedLogger = DefaultLogger.withTag("EduGo.Storage.Migrator")
) {

    /** Executes all pending migrations and returns the new schema version. */
    public fun migrate(): Int {
        val currentVersion = storage.getIntSafe(VERSION_KEY, 0)
        val sorted = migrations.sortedBy { it.version }

        val pending = sorted.filter { it.version > currentVersion }
        if (pending.isEmpty()) {
            logger.d("Already at version $currentVersion – nothing to migrate")
            return currentVersion
        }

        logger.i("Migrating from v$currentVersion → v${pending.last().version} (${pending.size} step(s))")

        for (migration in pending) {
            logger.d("Running migration v${migration.version}…")
            migration.migrate(storage)
            storage.putIntSafe(VERSION_KEY, migration.version)
            logger.d("Migration v${migration.version} complete")
        }

        val newVersion = storage.getIntSafe(VERSION_KEY, 0)
        logger.i("Migration finished – now at v$newVersion")
        return newVersion
    }

    /** Returns the current stored schema version without running migrations. */
    public fun currentVersion(): Int = storage.getIntSafe(VERSION_KEY, 0)

    public companion object {
        public const val VERSION_KEY: String = "storage.schema.version"
    }
}

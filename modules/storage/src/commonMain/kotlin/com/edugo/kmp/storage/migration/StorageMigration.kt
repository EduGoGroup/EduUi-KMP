package com.edugo.kmp.storage.migration

import com.edugo.kmp.storage.SafeEduGoStorage

/**
 * Represents a single storage schema migration step.
 *
 * Implementations define the [version] they migrate TO and the
 * [migrate] logic to transform stored data from the previous version.
 */
public interface StorageMigration {
    /** The schema version this migration upgrades to. */
    public val version: Int

    /** Executes the migration on the given [storage]. */
    public fun migrate(storage: SafeEduGoStorage)
}

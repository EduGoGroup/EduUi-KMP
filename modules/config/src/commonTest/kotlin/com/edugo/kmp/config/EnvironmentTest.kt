package com.edugo.kmp.config

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests para Environment enum.
 */
class EnvironmentTest {

    @Test
    fun fromString_parses_dev_case_insensitive() {
        assertEquals(Environment.DEV, Environment.fromString("dev"))
        assertEquals(Environment.DEV, Environment.fromString("DEV"))
        assertEquals(Environment.DEV, Environment.fromString("Dev"))
    }

    @Test
    fun fromString_parses_staging_case_insensitive() {
        assertEquals(Environment.STAGING, Environment.fromString("staging"))
        assertEquals(Environment.STAGING, Environment.fromString("STAGING"))
        assertEquals(Environment.STAGING, Environment.fromString("Staging"))
    }

    @Test
    fun fromString_parses_prod_case_insensitive() {
        assertEquals(Environment.PROD, Environment.fromString("prod"))
        assertEquals(Environment.PROD, Environment.fromString("PROD"))
        assertEquals(Environment.PROD, Environment.fromString("Prod"))
    }

    @Test
    fun fromString_returns_dev_for_invalid_values() {
        assertEquals(Environment.DEV, Environment.fromString("invalid"))
        assertEquals(Environment.DEV, Environment.fromString(null))
        assertEquals(Environment.DEV, Environment.fromString(""))
    }

    @Test
    fun fileName_returns_lowercase() {
        assertEquals("dev", Environment.DEV.fileName)
        assertEquals("staging", Environment.STAGING.fileName)
        assertEquals("prod", Environment.PROD.fileName)
    }

    @Test
    fun all_environments_exist() {
        val environments = Environment.entries
        assertEquals(3, environments.size)
        assertTrue(environments.contains(Environment.DEV))
        assertTrue(environments.contains(Environment.STAGING))
        assertTrue(environments.contains(Environment.PROD))
    }

    private fun assertTrue(value: Boolean) {
        kotlin.test.assertTrue(value)
    }
}

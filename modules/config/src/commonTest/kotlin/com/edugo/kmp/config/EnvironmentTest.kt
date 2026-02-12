package com.edugo.kmp.config

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EnvironmentTest {

    @Test
    fun fromStringConvertsCaseInsensitively() {
        assertEquals(Environment.DEV, Environment.fromString("DEV"))
        assertEquals(Environment.DEV, Environment.fromString("dev"))
        assertEquals(Environment.DEV, Environment.fromString("Dev"))
        assertEquals(Environment.DEV, Environment.fromString("dEv"))

        assertEquals(Environment.STAGING, Environment.fromString("STAGING"))
        assertEquals(Environment.STAGING, Environment.fromString("staging"))
        assertEquals(Environment.STAGING, Environment.fromString("Staging"))

        assertEquals(Environment.PROD, Environment.fromString("PROD"))
        assertEquals(Environment.PROD, Environment.fromString("prod"))
        assertEquals(Environment.PROD, Environment.fromString("Prod"))
    }

    @Test
    fun fromStringReturnsNullForInvalidStrings() {
        assertNull(Environment.fromString("invalid"))
        assertNull(Environment.fromString(""))
        assertNull(Environment.fromString("development"))
        assertNull(Environment.fromString("production"))
        assertNull(Environment.fromString("test"))
        assertNull(Environment.fromString("local"))
    }

    @Test
    fun fromStringReturnsNullForNullInput() {
        assertNull(Environment.fromString(null))
    }

    @Test
    fun fromStringOrDefaultReturnsEnvironmentForValidString() {
        assertEquals(Environment.DEV, Environment.fromStringOrDefault("dev"))
        assertEquals(Environment.STAGING, Environment.fromStringOrDefault("staging"))
        assertEquals(Environment.PROD, Environment.fromStringOrDefault("prod"))
    }

    @Test
    fun fromStringOrDefaultReturnsDefaultForInvalidString() {
        assertEquals(Environment.DEV, Environment.fromStringOrDefault("invalid"))
        assertEquals(Environment.DEV, Environment.fromStringOrDefault(null))
        assertEquals(Environment.STAGING, Environment.fromStringOrDefault("invalid", Environment.STAGING))
        assertEquals(Environment.PROD, Environment.fromStringOrDefault(null, Environment.PROD))
    }

    @Test
    fun fileNameReturnsLowercaseName() {
        assertEquals("dev", Environment.DEV.fileName)
        assertEquals("staging", Environment.STAGING.fileName)
        assertEquals("prod", Environment.PROD.fileName)
    }
}

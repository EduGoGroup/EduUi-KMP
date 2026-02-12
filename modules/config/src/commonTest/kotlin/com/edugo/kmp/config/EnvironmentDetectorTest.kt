package com.edugo.kmp.config

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class EnvironmentDetectorTest {

    @AfterTest
    fun cleanup() {
        EnvironmentDetector.reset()
    }

    @Test
    fun detectReturnsValidEnvironment() {
        val env = EnvironmentDetector.detect()
        assertNotNull(env, "Detected environment should not be null")
        assertTrue(
            env in listOf(Environment.DEV, Environment.STAGING, Environment.PROD),
            "Detected environment should be DEV STAGING or PROD but was $env"
        )
    }

    @Test
    fun forceEnvironmentOverridesDetection() {
        EnvironmentDetector.forceEnvironment(Environment.STAGING)

        val env = EnvironmentDetector.detect()

        assertEquals(Environment.STAGING, env, "Should return forced environment")
    }

    @Test
    fun resetRestoresAutomaticDetection() {
        EnvironmentDetector.forceEnvironment(Environment.PROD)
        assertEquals(Environment.PROD, EnvironmentDetector.detect())

        EnvironmentDetector.reset()

        val env = EnvironmentDetector.detect()
        assertNotNull(env, "After reset should still return valid environment")
    }

    @Test
    fun multipleForceCallsUsesLatest() {
        EnvironmentDetector.forceEnvironment(Environment.DEV)
        assertEquals(Environment.DEV, EnvironmentDetector.detect())

        EnvironmentDetector.forceEnvironment(Environment.STAGING)
        assertEquals(Environment.STAGING, EnvironmentDetector.detect())

        EnvironmentDetector.forceEnvironment(Environment.PROD)
        assertEquals(Environment.PROD, EnvironmentDetector.detect())
    }

    @Test
    fun forcedEnvironmentPersistsAcrossMultipleDetectCalls() {
        EnvironmentDetector.forceEnvironment(Environment.STAGING)

        repeat(5) {
            assertEquals(Environment.STAGING, EnvironmentDetector.detect())
        }
    }

    @Test
    fun resetIsIdempotent() {
        EnvironmentDetector.forceEnvironment(Environment.PROD)

        EnvironmentDetector.reset()
        val env1 = EnvironmentDetector.detect()

        EnvironmentDetector.reset()
        val env2 = EnvironmentDetector.detect()

        assertEquals(env1, env2, "Multiple resets should produce same result")
    }

    @Test
    fun detectWithoutForceUsesAutoDetection() {
        // No forceEnvironment called — should use platform detection
        val env = EnvironmentDetector.detect()
        assertTrue(
            env in listOf(Environment.DEV, Environment.STAGING, Environment.PROD),
            "Auto-detected environment should be valid"
        )
    }
}

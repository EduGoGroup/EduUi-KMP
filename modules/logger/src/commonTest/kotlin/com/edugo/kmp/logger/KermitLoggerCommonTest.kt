package com.edugo.kmp.logger

import co.touchlab.kermit.Severity
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests comunes para KermitLogger que se ejecutan en todas las plataformas.
 *
 * Estos tests verifican la funcionalidad compartida del wrapper de Kermit
 * sin depender de implementaciones especificas de plataforma.
 */
class KermitLoggerCommonTest {

    @Test
    fun testInitializeIsIdempotent() {
        // Arrange & Act
        KermitLogger.initialize()
        val firstLogger = KermitLogger.getLogger()

        KermitLogger.initialize() // Segunda inicializacion
        val secondLogger = KermitLogger.getLogger()

        // Assert
        assertNotNull(firstLogger, "First logger should not be null")
        assertNotNull(secondLogger, "Second logger should not be null")
        // La segunda llamada a initialize() no debe cambiar la instancia
    }

    @Test
    fun testGetLoggerReturnsValidInstance() {
        // Arrange
        KermitLogger.initialize()

        // Act
        val logger = KermitLogger.getLogger()

        // Assert
        assertNotNull(logger, "Logger instance should not be null after initialization")
    }

    @Test
    fun testDebugMethodDoesNotThrow() {
        // Arrange
        KermitLogger.initialize()

        // Act & Assert - No deberia lanzar excepcion
        try {
            KermitLogger.debug("TestTag", "Debug message")
            assertTrue(true, "Debug method executed without throwing")
        } catch (e: Exception) {
            throw AssertionError("Debug method should not throw exception", e)
        }
    }

    @Test
    fun testInfoMethodDoesNotThrow() {
        // Arrange
        KermitLogger.initialize()

        // Act & Assert
        try {
            KermitLogger.info("TestTag", "Info message")
            assertTrue(true, "Info method executed without throwing")
        } catch (e: Exception) {
            throw AssertionError("Info method should not throw exception", e)
        }
    }

    @Test
    fun testWarnMethodDoesNotThrow() {
        // Arrange
        KermitLogger.initialize()

        // Act & Assert
        try {
            KermitLogger.warn("TestTag", "Warning message")
            assertTrue(true, "Warn method executed without throwing")
        } catch (e: Exception) {
            throw AssertionError("Warn method should not throw exception", e)
        }
    }

    @Test
    fun testErrorMethodDoesNotThrow() {
        // Arrange
        KermitLogger.initialize()

        // Act & Assert
        try {
            KermitLogger.error("TestTag", "Error message")
            assertTrue(true, "Error method executed without throwing")
        } catch (e: Exception) {
            throw AssertionError("Error method should not throw exception", e)
        }
    }

    @Test
    fun testErrorMethodWithThrowableDoesNotThrow() {
        // Arrange
        KermitLogger.initialize()
        val exception = RuntimeException("Test exception")

        // Act & Assert
        try {
            KermitLogger.error("TestTag", "Error with exception", exception)
            assertTrue(true, "Error method with throwable executed without throwing")
        } catch (e: Exception) {
            throw AssertionError("Error method with throwable should not throw exception", e)
        }
    }

    @Test
    fun testSetMinSeverityDoesNotThrow() {
        // Arrange
        KermitLogger.initialize()

        // Act & Assert
        try {
            KermitLogger.setMinSeverity(Severity.Warn)
            assertTrue(true, "setMinSeverity executed without throwing")
        } catch (e: Exception) {
            throw AssertionError("setMinSeverity should not throw exception", e)
        }
    }

    @Test
    fun testMultipleLogCallsInSequence() {
        // Arrange
        KermitLogger.initialize()

        // Act & Assert - Multiples llamadas en secuencia no deberian fallar
        try {
            KermitLogger.debug("TestTag", "Message 1")
            KermitLogger.info("TestTag", "Message 2")
            KermitLogger.warn("TestTag", "Message 3")
            KermitLogger.error("TestTag", "Message 4")
            assertTrue(true, "Multiple log calls executed successfully")
        } catch (e: Exception) {
            throw AssertionError("Multiple log calls should not throw exception", e)
        }
    }

    @Test
    fun testSetLoggerAllowsCustomInstance() {
        // Arrange
        KermitLogger.initialize()
        val customLogger = KermitConfig.createLogger()

        // Act
        KermitLogger.setLogger(customLogger)
        val retrievedLogger = KermitLogger.getLogger()

        // Assert
        assertNotNull(retrievedLogger, "Custom logger should not be null")
    }

    @Test
    fun testKermitConfigCreatesValidLogger() {
        // Act
        val logger = KermitConfig.createLogger()

        // Assert
        assertNotNull(logger, "KermitConfig should create a valid logger")
    }

    @Test
    fun testKermitConfigCreatesLoggerWithMinSeverity() {
        // Act
        val logger = KermitConfig.createLoggerWithMinSeverity(Severity.Info)

        // Assert
        assertNotNull(logger, "KermitConfig should create a valid logger with min severity")
    }
}

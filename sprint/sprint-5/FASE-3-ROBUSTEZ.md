# Fase 3: Robustez y Producción

## Objetivo de la Fase

Endurecer el sistema de autenticación para producción, agregando reintentos inteligentes, circuit breaker, rate limiting, logs estructurados, y configuración por ambiente. Al finalizar esta fase, tendrás un sistema production-ready con todas las garantías necesarias.

**Entregable**: Sistema production-ready con robustez, observabilidad y configuración.

---

## Tiempo Estimado

**4-5 horas** distribuidas así:

- Reintentos y circuit breaker: 1.5 horas
- Rate limiting y throttling: 1 hora
- Logs estructurados: 0.5 horas
- Configuración por ambiente: 0.5 horas
- Tests de integración: 1-1.5 horas

---

## Prerequisitos

**OBLIGATORIO**: Fase 1 y Fase 2 completadas y funcionando correctamente.

Verificar:
- [ ] Login, logout, restoreSession funcionan
- [ ] Renovación automática funciona
- [ ] Interceptor HTTP funciona
- [ ] Todos los tests de Fase 1 y 2 pasan

---

## Tareas de Implementación

### Tarea 1: Implementar Política de Reintentos

**Archivo**: `/Users/jhoanmedina/source/EduGo/EduUI/kmp_new/modules/auth/src/commonMain/kotlin/com/edugo/kmp/auth/retry/RetryPolicy.kt`

```kotlin
package com.edugo.kmp.auth.retry

import com.edugo.kmp.foundation.result.Result
import com.edugo.kmp.logger.Logger
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Política de reintentos para operaciones de autenticación.
 */
data class RetryPolicy(
    val maxAttempts: Int = 3,
    val initialDelay: Duration = 500.milliseconds,
    val maxDelay: Duration = 5000.milliseconds,
    val backoffMultiplier: Double = 2.0,
    val retryableErrors: Set<String> = setOf(
        "network error",
        "timeout",
        "connection refused",
        "socket timeout"
    )
) {
    companion object {
        fun default() = RetryPolicy()
        
        fun aggressive() = RetryPolicy(
            maxAttempts = 5,
            initialDelay = 200.milliseconds,
            backoffMultiplier = 1.5
        )
        
        fun conservative() = RetryPolicy(
            maxAttempts = 2,
            initialDelay = 1000.milliseconds,
            backoffMultiplier = 3.0
        )
    }
}

/**
 * Ejecuta una operación con reintentos según la política.
 */
suspend fun <T> withRetry(
    policy: RetryPolicy = RetryPolicy.default(),
    operation: suspend (attempt: Int) -> Result<T>
): Result<T> {
    val logger = Logger.tagged("RetryPolicy")
    var currentDelay = policy.initialDelay
    var lastError: String? = null

    repeat(policy.maxAttempts) { attempt ->
        logger.debug("Attempt ${attempt + 1}/${policy.maxAttempts}")

        when (val result = operation(attempt + 1)) {
            is Result.Success -> {
                if (attempt > 0) {
                    logger.info("Operation succeeded after ${attempt + 1} attempts")
                }
                return result
            }
            is Result.Failure -> {
                lastError = result.error
                
                // Verificar si el error es reintentable
                val isRetryable = policy.retryableErrors.any { retryableError ->
                    result.error.lowercase().contains(retryableError.lowercase())
                }

                if (!isRetryable) {
                    logger.warn("Non-retryable error: ${result.error}")
                    return result
                }

                if (attempt < policy.maxAttempts - 1) {
                    logger.warn("Attempt ${attempt + 1} failed: ${result.error}. Retrying in ${currentDelay.inWholeMilliseconds}ms")
                    delay(currentDelay)
                    
                    // Exponential backoff
                    currentDelay = (currentDelay * policy.backoffMultiplier)
                        .coerceAtMost(policy.maxDelay)
                } else {
                    logger.error("All ${policy.maxAttempts} attempts failed. Last error: ${result.error}")
                }
            }
            is Result.Loading -> {
                // No deberíamos llegar aquí
                logger.warn("Unexpected loading state in retry")
            }
        }
    }

    return Result.Failure(lastError ?: "Operation failed after ${policy.maxAttempts} attempts")
}
```

**Validación**:
- Implementa exponential backoff correctamente
- Solo reintentan errores retryables
- Respeta máximo de intentos

---

### Tarea 2: Implementar Circuit Breaker

**Archivo**: `/Users/jhoanmedina/source/EduGo/EduUI/kmp_new/modules/auth/src/commonMain/kotlin/com/edugo/kmp/auth/circuit/CircuitBreaker.kt`

```kotlin
package com.edugo.kmp.auth.circuit

import com.edugo.kmp.foundation.result.Result
import com.edugo.kmp.foundation.result.failure
import com.edugo.kmp.logger.Logger
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Estados del circuit breaker.
 */
sealed class CircuitState {
    object Closed : CircuitState()
    data class Open(val openedAt: Instant) : CircuitState()
    data class HalfOpen(val attempt: Int) : CircuitState()
}

/**
 * Configuración del circuit breaker.
 */
data class CircuitBreakerConfig(
    val failureThreshold: Int = 5,
    val successThreshold: Int = 2,
    val timeout: Duration = 30.seconds,
    val halfOpenMaxAttempts: Int = 3
) {
    companion object {
        fun default() = CircuitBreakerConfig()
    }
}

/**
 * Circuit Breaker para prevenir cascadas de fallos.
 */
class CircuitBreaker(
    private val config: CircuitBreakerConfig = CircuitBreakerConfig.default(),
    private val name: String = "CircuitBreaker"
) {
    private val logger = Logger.tagged(name)
    
    private var state: CircuitState = CircuitState.Closed
    private var failureCount: Int = 0
    private var successCount: Int = 0

    /**
     * Ejecuta operación protegida por circuit breaker.
     */
    suspend fun <T> execute(operation: suspend () -> Result<T>): Result<T> {
        return when (val currentState = state) {
            is CircuitState.Closed -> executeClosed(operation)
            is CircuitState.Open -> executeOpen(currentState, operation)
            is CircuitState.HalfOpen -> executeHalfOpen(currentState, operation)
        }
    }

    private suspend fun <T> executeClosed(operation: suspend () -> Result<T>): Result<T> {
        val result = operation()

        when (result) {
            is Result.Success -> {
                reset()
            }
            is Result.Failure -> {
                recordFailure()
            }
            is Result.Loading -> {
                // No afecta al circuit breaker
            }
        }

        return result
    }

    private suspend fun <T> executeOpen(
        currentState: CircuitState.Open,
        operation: suspend () -> Result<T>
    ): Result<T> {
        val now = Clock.System.now()
        val elapsed = now - currentState.openedAt

        return if (elapsed >= config.timeout) {
            logger.info("Circuit breaker timeout expired, transitioning to half-open")
            state = CircuitState.HalfOpen(attempt = 1)
            executeHalfOpen(state as CircuitState.HalfOpen, operation)
        } else {
            val remainingSeconds = (config.timeout - elapsed).inWholeSeconds
            logger.warn("Circuit breaker is OPEN. Rejecting request. Retry in ${remainingSeconds}s")
            failure("Service temporarily unavailable. Retry in ${remainingSeconds}s")
        }
    }

    private suspend fun <T> executeHalfOpen(
        currentState: CircuitState.HalfOpen,
        operation: suspend () -> Result<T>
    ): Result<T> {
        if (currentState.attempt > config.halfOpenMaxAttempts) {
            logger.warn("Half-open max attempts exceeded, reopening circuit")
            open()
            return failure("Service temporarily unavailable")
        }

        val result = operation()

        when (result) {
            is Result.Success -> {
                successCount++
                logger.debug("Half-open success count: $successCount/${config.successThreshold}")
                
                if (successCount >= config.successThreshold) {
                    logger.info("Circuit breaker closing after successful recovery")
                    close()
                } else {
                    state = CircuitState.HalfOpen(attempt = currentState.attempt + 1)
                }
            }
            is Result.Failure -> {
                logger.warn("Half-open attempt failed, reopening circuit")
                open()
            }
            is Result.Loading -> {
                // No afecta
            }
        }

        return result
    }

    private fun recordFailure() {
        failureCount++
        logger.debug("Failure count: $failureCount/${config.failureThreshold}")

        if (failureCount >= config.failureThreshold) {
            logger.warn("Failure threshold reached, opening circuit breaker")
            open()
        }
    }

    private fun open() {
        state = CircuitState.Open(Clock.System.now())
        failureCount = 0
        successCount = 0
    }

    private fun close() {
        state = CircuitState.Closed
        failureCount = 0
        successCount = 0
    }

    private fun reset() {
        failureCount = 0
        successCount = 0
    }

    fun getState(): CircuitState = state
    
    fun forceOpen() {
        logger.warn("Circuit breaker force-opened")
        open()
    }
    
    fun forceClose() {
        logger.info("Circuit breaker force-closed")
        close()
    }
}
```

**Validación**:
- Implementa estados correctamente (Closed, Open, HalfOpen)
- Previene sobrecarga del servidor
- Permite recuperación gradual

---

### Tarea 3: Actualizar AuthRepository con Reintentos y Circuit Breaker

**Archivo**: `/Users/jhoanmedina/source/EduGo/EduUI/kmp_new/modules/auth/src/commonMain/kotlin/com/edugo/kmp/auth/repository/AuthRepositoryImpl.kt`

Actualizar para usar retry y circuit breaker:

```kotlin
package com.edugo.kmp.auth.repository

import com.edugo.kmp.auth.circuit.CircuitBreaker
import com.edugo.kmp.auth.circuit.CircuitBreakerConfig
import com.edugo.kmp.auth.model.LoginCredentials
import com.edugo.kmp.auth.model.LoginResponse
import com.edugo.kmp.auth.retry.RetryPolicy
import com.edugo.kmp.auth.retry.withRetry
import com.edugo.kmp.foundation.result.Result
import com.edugo.kmp.foundation.result.failure
import com.edugo.kmp.foundation.result.success
import com.edugo.kmp.logger.Logger
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * Implementación robusta de AuthRepository con reintentos y circuit breaker.
 */
class AuthRepositoryImpl(
    private val httpClient: HttpClient,
    private val baseUrl: String,
    private val retryPolicy: RetryPolicy = RetryPolicy.default(),
    private val circuitBreaker: CircuitBreaker = CircuitBreaker(
        config = CircuitBreakerConfig.default(),
        name = "AuthRepository"
    )
) : AuthRepository {

    private val logger = Logger.tagged("AuthRepository")

    override suspend fun login(credentials: LoginCredentials): Result<LoginResponse> {
        return circuitBreaker.execute {
            withRetry(retryPolicy) { attempt ->
                try {
                    logger.debug("Login attempt $attempt for user: ${credentials.username}")
                    
                    val response = httpClient.post("$baseUrl/auth/login") {
                        contentType(ContentType.Application.Json)
                        setBody(credentials)
                    }

                    when (response.status) {
                        HttpStatusCode.OK -> {
                            val loginResponse = response.body<LoginResponse>()
                            logger.info("Login successful for user: ${credentials.username}")
                            success(loginResponse)
                        }
                        HttpStatusCode.Unauthorized -> {
                            logger.warn("Login failed: Invalid credentials")
                            failure("Credenciales inválidas")
                        }
                        HttpStatusCode.TooManyRequests -> {
                            logger.warn("Login rate limited")
                            failure("Demasiados intentos. Intenta más tarde")
                        }
                        else -> {
                            logger.error("Login failed with status: ${response.status}")
                            failure("Error en el servidor: ${response.status}")
                        }
                    }
                } catch (e: Exception) {
                    logger.error("Login failed with exception: ${e.message}")
                    failure("Error de conexión: ${e.message ?: "Unknown error"}")
                }
            }
        }
    }

    override suspend fun refreshToken(refreshToken: String): Result<RefreshResponse> {
        return circuitBreaker.execute {
            withRetry(retryPolicy) { attempt ->
                try {
                    logger.debug("Token refresh attempt $attempt")
                    
                    val response = httpClient.post("$baseUrl/auth/refresh") {
                        contentType(ContentType.Application.Json)
                        setBody(mapOf("refresh_token" to refreshToken))
                    }

                    when (response.status) {
                        HttpStatusCode.OK -> {
                            val refreshResponse = response.body<RefreshResponse>()
                            logger.info("Token refresh successful")
                            success(refreshResponse)
                        }
                        HttpStatusCode.Unauthorized -> {
                            logger.warn("Token refresh failed: Invalid refresh token")
                            failure("Sesión expirada")
                        }
                        else -> {
                            logger.error("Token refresh failed with status: ${response.status}")
                            failure("Error al renovar token: ${response.status}")
                        }
                    }
                } catch (e: Exception) {
                    logger.error("Token refresh failed with exception: ${e.message}")
                    failure("Error de conexión: ${e.message ?: "Unknown error"}")
                }
            }
        }
    }

    override suspend fun verifyToken(token: String): Result<TokenVerificationResponse> {
        // Verify no usa reintentos (es rápido y no crítico)
        return try {
            logger.debug("Verifying token")
            
            val response = httpClient.post("$baseUrl/auth/verify") {
                contentType(ContentType.Application.Json)
                bearerAuth(token)
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    val verificationResponse = response.body<TokenVerificationResponse>()
                    logger.debug("Token verification successful")
                    success(verificationResponse)
                }
                HttpStatusCode.Unauthorized -> {
                    logger.warn("Token verification failed: Invalid token")
                    failure("Token inválido")
                }
                else -> {
                    logger.error("Token verification failed with status: ${response.status}")
                    failure("Error al verificar token: ${response.status}")
                }
            }
        } catch (e: Exception) {
            logger.error("Token verification failed with exception: ${e.message}")
            failure("Error de conexión: ${e.message ?: "Unknown error"}")
        }
    }

    override suspend fun logout(token: String): Result<Unit> {
        // Logout no usa circuit breaker (siempre debe funcionar localmente)
        return try {
            logger.debug("Attempting logout")
            
            val response = httpClient.post("$baseUrl/auth/logout") {
                bearerAuth(token)
            }

            when (response.status) {
                HttpStatusCode.OK, HttpStatusCode.NoContent -> {
                    logger.info("Logout successful")
                    success(Unit)
                }
                else -> {
                    logger.warn("Logout failed with status: ${response.status}")
                    // Logout siempre retorna success localmente
                    success(Unit)
                }
            }
        } catch (e: Exception) {
            logger.warn("Logout failed with exception: ${e.message}")
            // Logout siempre retorna success localmente
            success(Unit)
        }
    }
}
```

**Validación**:
- Login y refresh usan reintentos
- Circuit breaker protege operaciones críticas
- Logout siempre funciona localmente

---

### Tarea 4: Implementar Rate Limiting Client-Side

**Archivo**: `/Users/jhoanmedina/source/EduGo/EduUI/kmp_new/modules/auth/src/commonMain/kotlin/com/edugo/kmp/auth/throttle/RateLimiter.kt`

```kotlin
package com.edugo.kmp.auth.throttle

import com.edugo.kmp.foundation.result.Result
import com.edugo.kmp.foundation.result.failure
import com.edugo.kmp.logger.Logger
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Rate limiter para prevenir abuso de operaciones.
 */
class RateLimiter(
    private val maxRequests: Int = 5,
    private val timeWindow: Duration = 60.seconds,
    private val name: String = "RateLimiter"
) {
    private val logger = Logger.tagged(name)
    private val requestTimestamps = mutableListOf<Instant>()

    /**
     * Verifica si la operación puede ejecutarse según rate limit.
     */
    suspend fun <T> execute(operation: suspend () -> Result<T>): Result<T> {
        cleanup()

        if (requestTimestamps.size >= maxRequests) {
            val oldestRequest = requestTimestamps.first()
            val waitTime = timeWindow - (Clock.System.now() - oldestRequest)
            
            logger.warn("Rate limit exceeded. Wait ${waitTime.inWholeSeconds}s")
            return failure("Demasiadas solicitudes. Espera ${waitTime.inWholeSeconds} segundos")
        }

        requestTimestamps.add(Clock.System.now())
        return operation()
    }

    /**
     * Limpia timestamps antiguos fuera de la ventana.
     */
    private fun cleanup() {
        val now = Clock.System.now()
        requestTimestamps.removeAll { timestamp ->
            (now - timestamp) > timeWindow
        }
    }

    /**
     * Resetea el rate limiter.
     */
    fun reset() {
        requestTimestamps.clear()
        logger.debug("Rate limiter reset")
    }

    /**
     * Obtiene requests restantes.
     */
    fun remainingRequests(): Int {
        cleanup()
        return maxOf(0, maxRequests - requestTimestamps.size)
    }
}
```

**Validación**:
- Limita requests por ventana de tiempo
- Limpia timestamps antiguos correctamente
- Provee información de requests restantes

---

### Tarea 5: Agregar Logs Estructurados

**Archivo**: `/Users/jhoanmedina/source/EduGo/EduUI/kmp_new/modules/auth/src/commonMain/kotlin/com/edugo/kmp/auth/logging/AuthLogger.kt`

```kotlin
package com.edugo.kmp.auth.logging

import com.edugo.kmp.logger.Logger

/**
 * Logger estructurado para operaciones de autenticación.
 */
object AuthLogger {
    private val logger = Logger.tagged("Auth")

    fun logLoginAttempt(username: String) {
        logger.info("LOGIN_ATTEMPT | user=$username")
    }

    fun logLoginSuccess(username: String, userId: String) {
        logger.info("LOGIN_SUCCESS | user=$username | userId=$userId")
    }

    fun logLoginFailure(username: String, reason: String) {
        logger.warn("LOGIN_FAILURE | user=$username | reason=$reason")
    }

    fun logLogout(userId: String, reason: String = "user_action") {
        logger.info("LOGOUT | userId=$userId | reason=$reason")
    }

    fun logTokenRefresh(userId: String, success: Boolean) {
        if (success) {
            logger.info("TOKEN_REFRESH_SUCCESS | userId=$userId")
        } else {
            logger.warn("TOKEN_REFRESH_FAILURE | userId=$userId")
        }
    }

    fun logSessionRestore(userId: String, success: Boolean) {
        if (success) {
            logger.info("SESSION_RESTORE_SUCCESS | userId=$userId")
        } else {
            logger.warn("SESSION_RESTORE_FAILURE | userId=$userId")
        }
    }

    fun logSessionExpired(userId: String, reason: String) {
        logger.warn("SESSION_EXPIRED | userId=$userId | reason=$reason")
    }

    fun logSecurityEvent(event: String, details: Map<String, String> = emptyMap()) {
        val detailsStr = details.entries.joinToString(" | ") { "${it.key}=${it.value}" }
        logger.warn("SECURITY_EVENT | event=$event | $detailsStr")
    }
}
```

**Validación**:
- Logs estructurados y parseables
- Contienen información relevante
- No loguean información sensible

---

### Tarea 6: Configuración por Ambiente

**Archivo**: `/Users/jhoanmedina/source/EduGo/EduUI/kmp_new/modules/auth/src/commonMain/kotlin/com/edugo/kmp/auth/config/AuthConfig.kt`

```kotlin
package com.edugo.kmp.auth.config

import com.edugo.kmp.auth.circuit.CircuitBreakerConfig
import com.edugo.kmp.auth.retry.RetryPolicy
import com.edugo.kmp.auth.token.TokenRefreshConfig
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Configuración de autenticación por ambiente.
 */
data class AuthConfig(
    val retryPolicy: RetryPolicy,
    val circuitBreakerConfig: CircuitBreakerConfig,
    val tokenRefreshConfig: TokenRefreshConfig,
    val rateLimitMaxRequests: Int,
    val enableDetailedLogs: Boolean
) {
    companion object {
        /**
         * Configuración para desarrollo.
         */
        fun development() = AuthConfig(
            retryPolicy = RetryPolicy(
                maxAttempts = 2,
                initialDelay = 500.milliseconds
            ),
            circuitBreakerConfig = CircuitBreakerConfig(
                failureThreshold = 10,
                timeout = 10.seconds
            ),
            tokenRefreshConfig = TokenRefreshConfig(
                refreshThresholdPercent = 50.0,
                minRefreshInterval = 5000
            ),
            rateLimitMaxRequests = 20,
            enableDetailedLogs = true
        )

        /**
         * Configuración para producción.
         */
        fun production() = AuthConfig(
            retryPolicy = RetryPolicy.default(),
            circuitBreakerConfig = CircuitBreakerConfig.default(),
            tokenRefreshConfig = TokenRefreshConfig.default(),
            rateLimitMaxRequests = 5,
            enableDetailedLogs = false
        )

        /**
         * Configuración para testing.
         */
        fun testing() = AuthConfig(
            retryPolicy = RetryPolicy(
                maxAttempts = 1,
                initialDelay = 100.milliseconds
            ),
            circuitBreakerConfig = CircuitBreakerConfig(
                failureThreshold = 3,
                timeout = 5.seconds
            ),
            tokenRefreshConfig = TokenRefreshConfig(
                refreshThresholdPercent = 90.0,
                minRefreshInterval = 1000
            ),
            rateLimitMaxRequests = 100,
            enableDetailedLogs = true
        )
    }
}
```

**Validación**:
- Configuraciones específicas por ambiente
- Valores sensibles para producción
- Valores permisivos para desarrollo

---

## Tests de Fase 3

### Test 1: RetryPolicy Test

**Archivo**: `/Users/jhoanmedina/source/EduGo/EduUI/kmp_new/modules/auth/src/commonTest/kotlin/com/edugo/kmp/auth/retry/RetryPolicyTest.kt`

```kotlin
package com.edugo.kmp.auth.retry

import com.edugo.kmp.foundation.result.Result
import com.edugo.kmp.foundation.result.failure
import com.edugo.kmp.foundation.result.success
import kotlinx.coroutines.test.runTest
import kotlin.test.*
import kotlin.time.Duration.Companion.milliseconds

class RetryPolicyTest {

    @Test
    fun `withRetry succeeds on first attempt`() = runTest {
        val policy = RetryPolicy(maxAttempts = 3)
        var attemptCount = 0

        val result = withRetry(policy) { attempt ->
            attemptCount = attempt
            success("success")
        }

        assertTrue(result is Result.Success)
        assertEquals(1, attemptCount)
    }

    @Test
    fun `withRetry retries on retryable error`() = runTest {
        val policy = RetryPolicy(
            maxAttempts = 3,
            initialDelay = 10.milliseconds,
            retryableErrors = setOf("network error")
        )
        var attemptCount = 0

        val result = withRetry(policy) { attempt ->
            attemptCount = attempt
            if (attempt < 3) {
                failure("network error occurred")
            } else {
                success("success")
            }
        }

        assertTrue(result is Result.Success)
        assertEquals(3, attemptCount)
    }

    @Test
    fun `withRetry does not retry on non-retryable error`() = runTest {
        val policy = RetryPolicy(
            maxAttempts = 3,
            retryableErrors = setOf("network error")
        )
        var attemptCount = 0

        val result = withRetry(policy) { attempt ->
            attemptCount = attempt
            failure("invalid credentials")
        }

        assertTrue(result is Result.Failure)
        assertEquals(1, attemptCount)
    }

    @Test
    fun `withRetry respects max attempts`() = runTest {
        val policy = RetryPolicy(
            maxAttempts = 3,
            initialDelay = 10.milliseconds,
            retryableErrors = setOf("network error")
        )
        var attemptCount = 0

        val result = withRetry(policy) { attempt ->
            attemptCount = attempt
            failure("network error")
        }

        assertTrue(result is Result.Failure)
        assertEquals(3, attemptCount)
    }
}
```

---

### Test 2: CircuitBreaker Test

**Archivo**: `/Users/jhoanmedina/source/EduGo/EduUI/kmp_new/modules/auth/src/commonTest/kotlin/com/edugo/kmp/auth/circuit/CircuitBreakerTest.kt`

```kotlin
package com.edugo.kmp.auth.circuit

import com.edugo.kmp.foundation.result.Result
import com.edugo.kmp.foundation.result.failure
import com.edugo.kmp.foundation.result.success
import kotlinx.coroutines.test.runTest
import kotlin.test.*
import kotlin.time.Duration.Companion.milliseconds

class CircuitBreakerTest {

    @Test
    fun `circuit breaker starts in closed state`() {
        val breaker = CircuitBreaker()
        assertTrue(breaker.getState() is CircuitState.Closed)
    }

    @Test
    fun `circuit breaker opens after failure threshold`() = runTest {
        val config = CircuitBreakerConfig(failureThreshold = 3)
        val breaker = CircuitBreaker(config)

        repeat(3) {
            breaker.execute { failure<String>("error") }
        }

        assertTrue(breaker.getState() is CircuitState.Open)
    }

    @Test
    fun `circuit breaker rejects requests when open`() = runTest {
        val config = CircuitBreakerConfig(
            failureThreshold = 2,
            timeout = 100.milliseconds
        )
        val breaker = CircuitBreaker(config)

        // Abrir el circuit breaker
        repeat(2) {
            breaker.execute { failure<String>("error") }
        }

        // Intentar ejecutar - debe ser rechazado
        val result = breaker.execute { success("data") }
        
        assertTrue(result is Result.Failure)
        assertTrue(result.error.contains("temporarily unavailable"))
    }

    @Test
    fun `circuit breaker transitions to half-open after timeout`() = runTest {
        val config = CircuitBreakerConfig(
            failureThreshold = 2,
            timeout = 10.milliseconds
        )
        val breaker = CircuitBreaker(config)

        // Abrir circuit breaker
        repeat(2) {
            breaker.execute { failure<String>("error") }
        }

        // Esperar timeout
        kotlinx.coroutines.delay(20)

        // Próxima ejecución debe entrar en half-open
        breaker.execute { success("data") }
        
        val state = breaker.getState()
        assertTrue(state is CircuitState.HalfOpen || state is CircuitState.Closed)
    }

    @Test
    fun `circuit breaker closes after successful recovery`() = runTest {
        val config = CircuitBreakerConfig(
            failureThreshold = 2,
            successThreshold = 2,
            timeout = 10.milliseconds
        )
        val breaker = CircuitBreaker(config)

        // Abrir
        repeat(2) {
            breaker.execute { failure<String>("error") }
        }

        // Esperar timeout
        kotlinx.coroutines.delay(20)

        // Successes en half-open
        repeat(2) {
            breaker.execute { success("data") }
        }

        assertTrue(breaker.getState() is CircuitState.Closed)
    }
}
```

---

### Test 3: RateLimiter Test

**Archivo**: `/Users/jhoanmedina/source/EduGo/EduUI/kmp_new/modules/auth/src/commonTest/kotlin/com/edugo/kmp/auth/throttle/RateLimiterTest.kt`

```kotlin
package com.edugo.kmp.auth.throttle

import com.edugo.kmp.foundation.result.Result
import com.edugo.kmp.foundation.result.success
import kotlinx.coroutines.test.runTest
import kotlin.test.*
import kotlin.time.Duration.Companion.milliseconds

class RateLimiterTest {

    @Test
    fun `allows requests within limit`() = runTest {
        val limiter = RateLimiter(
            maxRequests = 3,
            timeWindow = 100.milliseconds
        )

        repeat(3) { index ->
            val result = limiter.execute { success("request-$index") }
            assertTrue(result is Result.Success)
        }
    }

    @Test
    fun `blocks requests exceeding limit`() = runTest {
        val limiter = RateLimiter(
            maxRequests = 2,
            timeWindow = 100.milliseconds
        )

        // Primeras 2 deben pasar
        repeat(2) {
            val result = limiter.execute { success("ok") }
            assertTrue(result is Result.Success)
        }

        // Tercera debe fallar
        val result = limiter.execute { success("should-fail") }
        assertTrue(result is Result.Failure)
    }

    @Test
    fun `allows requests after time window expires`() = runTest {
        val limiter = RateLimiter(
            maxRequests = 2,
            timeWindow = 50.milliseconds
        )

        // Llenar límite
        repeat(2) {
            limiter.execute { success("ok") }
        }

        // Esperar que expire ventana
        kotlinx.coroutines.delay(60)

        // Debe permitir nueva request
        val result = limiter.execute { success("ok") }
        assertTrue(result is Result.Success)
    }

    @Test
    fun `remainingRequests returns correct value`() = runTest {
        val limiter = RateLimiter(maxRequests = 5)

        assertEquals(5, limiter.remainingRequests())

        limiter.execute { success("ok") }
        assertEquals(4, limiter.remainingRequests())

        limiter.execute { success("ok") }
        assertEquals(3, limiter.remainingRequests())
    }
}
```

---

## Criterios de Aceptación de Fase 3

### Funcionalidad
- [ ] Reintentos funcionan con exponential backoff
- [ ] Circuit breaker previene cascadas de fallos
- [ ] Rate limiter previene abuso
- [ ] Logs estructurados y parseables
- [ ] Configuración por ambiente funciona

### Testing
- [ ] RetryPolicyTest pasa todos los tests
- [ ] CircuitBreakerTest pasa todos los tests
- [ ] RateLimiterTest pasa todos los tests
- [ ] Tests de Fase 1 y 2 siguen pasando
- [ ] Cobertura de tests > 80%

### Compilación
- [ ] `./gradlew :modules:auth:build` exitoso
- [ ] Cero warnings
- [ ] Cero errores de lint

### Production-Ready
- [ ] No se loguean passwords ni tokens completos
- [ ] Errores de red no rompen la app
- [ ] Circuit breaker protege el servidor
- [ ] Rate limiting previene abuso

---

## Checklist de Validación

```bash
# 1. Compilar
./gradlew :modules:auth:build

# 2. Ejecutar todos los tests
./gradlew :modules:auth:test

# 3. Verificar cobertura
./gradlew :modules:auth:testDebugUnitTestCoverage

# 4. Lint
./gradlew :modules:auth:lintKotlin

# 5. Verificar warnings
./gradlew :modules:auth:build --warning-mode all
```

**Todos deben pasar** para considerar el sistema production-ready.

---

## Cómo Probar Manualmente

### Test 1: Reintentos

```kotlin
// Simular red inestable
lifecycleScope.launch {
    val result = authService.login(
        LoginCredentials("user@edugo.com", "password")
    )
    // Observar logs para ver reintentos
}
```

### Test 2: Circuit Breaker

```kotlin
// Simular servidor caído
lifecycleScope.launch {
    repeat(10) {
        authService.login(LoginCredentials("user@edugo.com", "wrong"))
        delay(500)
    }
    // Después de 5 fallos, circuit breaker debe abrirse
}
```

### Test 3: Rate Limiting

```kotlin
lifecycleScope.launch {
    repeat(10) {
        authService.login(LoginCredentials("user@edugo.com", "password"))
    }
    // Debe rechazar después de 5 requests
}
```

---

## Problemas Comunes

### Error: "Too many retries"
**Causa**: RetryPolicy muy agresivo  
**Solución**: Ajustar `maxAttempts` o `backoffMultiplier`

### Error: Circuit breaker no se cierra
**Causa**: `successThreshold` no alcanzado  
**Solución**: Verificar que hay suficientes requests exitosos

### Error: Rate limit muy restrictivo
**Causa**: `maxRequests` o `timeWindow` muy bajos  
**Solución**: Ajustar configuración según ambiente

---

## Documentos Relacionados

- [ARQUITECTURA.md](ARQUITECTURA.md) - Arquitectura completa del sistema
- [TESTING.md](TESTING.md) - Estrategia de testing
- [TROUBLESHOOTING.md](TROUBLESHOOTING.md) - Solución de problemas

---

## Sistema Completo

**¡Felicitaciones!** Al completar Fase 3, tienes un sistema de autenticación production-ready con:

- Login/Logout funcional
- Renovación automática de tokens
- Interceptor HTTP
- Reintentos inteligentes
- Circuit breaker
- Rate limiting
- Logs estructurados
- Configuración por ambiente
- Cobertura de tests > 80%

El sistema está listo para producción.

---

**Fase**: 3 - Robustez  
**Estado**: Listo para implementar  
**Prerequisito**: Fase 1 y 2 completadas  
**Sistema**: Production-ready

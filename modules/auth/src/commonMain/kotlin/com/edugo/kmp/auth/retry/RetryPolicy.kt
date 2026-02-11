package com.edugo.kmp.auth.retry

import com.edugo.kmp.foundation.result.Result
import com.edugo.kmp.logger.Logger
import kotlinx.coroutines.delay
import kotlin.math.pow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Politica de reintentos con exponential backoff para operaciones de autenticacion.
 *
 * Solo reintenta errores transitorios de red; errores de negocio (credenciales
 * invalidas, token revocado, etc.) se propagan inmediatamente.
 *
 * @property maxAttempts Numero maximo de intentos (incluido el primero)
 * @property initialDelay Delay antes del primer reintento
 * @property maxDelay Techo maximo para el delay (evita esperas excesivas)
 * @property backoffMultiplier Factor multiplicador entre reintentos
 * @property retryableErrors Substrings que identifican errores transitorios
 */
public data class RetryPolicy(
    val maxAttempts: Int = 3,
    val initialDelay: Duration = 500.milliseconds,
    val maxDelay: Duration = 5.seconds,
    val backoffMultiplier: Double = 2.0,
    val retryableErrors: Set<String> = DEFAULT_RETRYABLE_ERRORS
) {
    init {
        require(maxAttempts >= 1) { "maxAttempts debe ser >= 1, fue: $maxAttempts" }
        require(initialDelay.isPositive()) { "initialDelay debe ser positivo" }
        require(maxDelay >= initialDelay) { "maxDelay debe ser >= initialDelay" }
        require(backoffMultiplier >= 1.0) { "backoffMultiplier debe ser >= 1.0, fue: $backoffMultiplier" }
    }

    /**
     * Calcula el delay para un intento dado (1-indexed).
     */
    public fun delayForAttempt(attempt: Int): Duration {
        if (attempt <= 1) return initialDelay
        val factor = backoffMultiplier.pow((attempt - 1))
        val calculated = (initialDelay.inWholeMilliseconds * factor).toLong().milliseconds
        return minOf(calculated, maxDelay)
    }

    /**
     * Determina si un error es retryable basandose en los substrings configurados.
     */
    public fun isRetryable(errorMessage: String): Boolean {
        val lower = errorMessage.lowercase()
        return retryableErrors.any { lower.contains(it.lowercase()) }
    }

    public companion object {
        private val DEFAULT_RETRYABLE_ERRORS: Set<String> = setOf(
            "network",
            "timeout",
            "connection",
            "socket",
            "unreachable",
            "reset",
            "503",
            "502"
        )

        public val DEFAULT: RetryPolicy = RetryPolicy()

        public val AGGRESSIVE: RetryPolicy = RetryPolicy(
            maxAttempts = 5,
            initialDelay = 200.milliseconds,
            maxDelay = 3.seconds,
            backoffMultiplier = 1.5
        )

        public val CONSERVATIVE: RetryPolicy = RetryPolicy(
            maxAttempts = 2,
            initialDelay = 1.seconds,
            maxDelay = 10.seconds,
            backoffMultiplier = 3.0
        )

        public val NO_RETRY: RetryPolicy = RetryPolicy(
            maxAttempts = 1,
            initialDelay = 1.milliseconds,
            maxDelay = 1.milliseconds
        )
    }
}

/**
 * Ejecuta una operacion con reintentos segun la politica dada.
 *
 * - Primer intento se ejecuta inmediatamente.
 * - Reintentos subsecuentes aplican exponential backoff.
 * - Solo reintenta si el error es retryable segun la politica.
 * - Errores no retryables se retornan inmediatamente.
 *
 * @param policy Politica de reintentos
 * @param logger Logger opcional (usa DefaultLogger si no se provee)
 * @param operation Operacion suspendible que retorna Result<T>
 * @return El primer Result.Success, o el ultimo Result.Failure si se agotan los intentos
 */
public suspend fun <T> withRetry(
    policy: RetryPolicy = RetryPolicy.DEFAULT,
    logger: Logger? = null,
    operation: suspend (attempt: Int) -> Result<T>
): Result<T> {
    val tag = "RetryPolicy"
    var lastError: String? = null

    repeat(policy.maxAttempts) { index ->
        val attempt = index + 1

        if (attempt > 1) {
            val delayDuration = policy.delayForAttempt(attempt)
            logger?.d(tag, "Retry attempt $attempt/${policy.maxAttempts} in ${delayDuration.inWholeMilliseconds}ms")
            delay(delayDuration)
        }

        when (val result = operation(attempt)) {
            is Result.Success -> {
                if (attempt > 1) {
                    logger?.i(tag, "Operation succeeded after $attempt attempts")
                }
                return result
            }
            is Result.Failure -> {
                lastError = result.error

                if (!policy.isRetryable(result.error)) {
                    logger?.w(tag, "Non-retryable error: ${result.error}")
                    return result
                }

                if (attempt == policy.maxAttempts) {
                    logger?.e(tag, "All $attempt attempts exhausted. Last error: ${result.error}")
                } else {
                    logger?.w(tag, "Attempt $attempt failed: ${result.error}")
                }
            }
            is Result.Loading -> {
                // No deberia ocurrir en suspend function
            }
        }
    }

    return Result.Failure(lastError ?: "Operation failed after ${policy.maxAttempts} attempts")
}

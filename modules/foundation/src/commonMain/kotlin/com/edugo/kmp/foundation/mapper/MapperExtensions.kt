package com.edugo.kmp.foundation.mapper

import com.edugo.kmp.foundation.error.AppError
import com.edugo.kmp.foundation.error.ErrorCode
import com.edugo.kmp.foundation.result.Result
import com.edugo.kmp.foundation.result.flatMap
import kotlin.time.measureTimedValue

/**
 * Extensiones para integrar DomainMapper con AppError y Result.
 *
 * Estas extensiones facilitan la conversión entre DTOs y modelos de dominio
 * con manejo de errores estructurado usando AppError y ErrorCode.
 *
 * ## Características
 *
 * - **Integración con AppError**: Convierte errores de validación a AppError
 * - **ErrorCode Type-Safe**: Usa códigos de error tipados
 * - **Metadata Enriquecida**: Agrega contexto adicional a los errores
 * - **Composable**: Se puede encadenar con otras operaciones Result
 *
 * ## Uso Básico
 *
 * ```kotlin
 * val dto = UserDto(email = "invalid", age = 15)
 * val result = dto.toDomainWithAppError(UserMapper)
 *
 * when (result) {
 *     is Result.Success -> println("User: ${result.data}")
 *     is Result.Failure -> {
 *         val appError = result.toAppError()
 *         println("Error: ${appError.code} - ${appError.message}")
 *     }
 * }
 * ```
 */

/**
 * Convierte un DTO a dominio y envuelve errores en AppError.
 *
 * Si la conversión falla, convierte el error de validación en un AppError
 * con código VALIDATION_ERROR y metadata adicional.
 *
 * @param mapper Mapper a usar para la conversión
 * @param errorCode Código de error a usar si la validación falla (default: VALIDATION_ERROR)
 * @param details Metadata adicional a incluir en el AppError
 * @return Result con el dominio o un Failure con mensaje de AppError
 */
public fun <DTO, Domain> DTO.toDomainWithAppError(
    mapper: DomainMapper<DTO, Domain>,
    errorCode: ErrorCode = ErrorCode.VALIDATION_INVALID_INPUT,
    details: Map<String, String> = emptyMap()
): Result<Domain> {
    return when (val result = mapper.toDomain(this)) {
        is Result.Success -> result
        is Result.Failure -> {
            val appError = AppError(
                code = errorCode,
                message = result.error,
                detailsInternal = details,
                cause = null
            )
            val detailedMessage = buildString {
                append("[${appError.code.name}] ${appError.message}")
                if (appError.details.isNotEmpty()) {
                    append(" (")
                    append(appError.details.entries.joinToString(", ") { "${it.key}=${it.value}" })
                    append(")")
                }
            }
            Result.Failure(detailedMessage)
        }
        is Result.Loading -> Result.Loading
    }
}

/**
 * Convierte una lista de DTOs a dominios con manejo de AppError.
 *
 * Si alguna conversión falla, retorna el primer error como AppError.
 *
 * @param mapper Mapper a usar para cada conversión
 * @param errorCode Código de error base (se enriquece con índice del item)
 * @param includeIndex Si true, agrega el índice del item que falló a los details
 * @return Result con lista de dominios o Failure con AppError
 */
public fun <DTO, Domain> List<DTO>.toDomainListWithAppError(
    mapper: DomainMapper<DTO, Domain>,
    errorCode: ErrorCode = ErrorCode.VALIDATION_INVALID_INPUT,
    includeIndex: Boolean = true
): Result<List<Domain>> {
    val domains = mutableListOf<Domain>()
    forEachIndexed { index, dto ->
        val details = if (includeIndex) {
            mapOf("index" to index.toString(), "total" to size.toString())
        } else {
            emptyMap()
        }

        when (val result = dto.toDomainWithAppError(mapper, errorCode, details)) {
            is Result.Success -> domains.add(result.data)
            is Result.Failure -> return result
            is Result.Loading -> return Result.Loading
        }
    }
    return Result.Success(domains)
}

/**
 * Mapea un Result<Domain> a otro tipo usando una transformación.
 *
 * Útil para encadenar transformaciones después de convertir de DTO a Domain.
 *
 * @param transform Función de transformación del dominio
 * @return Result con el dominio transformado
 */
public inline fun <Domain, R> Result<Domain>.mapDomain(
    crossinline transform: (Domain) -> R
): Result<R> {
    return when (this) {
        is Result.Success -> Result.Success(transform(data))
        is Result.Failure -> this
        is Result.Loading -> Result.Loading
    }
}

/**
 * FlatMap específico para dominios que retorna Result.
 *
 * Útil para encadenar operaciones que pueden fallar después de la conversión.
 *
 * @param transform Función que transforma el dominio y retorna otro Result
 * @return Result con el resultado de la transformación
 */
public inline fun <Domain, R> Result<Domain>.flatMapDomain(
    crossinline transform: (Domain) -> Result<R>
): Result<R> {
    return flatMap { transform(it) }
}

/**
 * Valida y convierte un DTO a dominio con validación personalizada adicional.
 *
 * Permite agregar validaciones adicionales después de la conversión del mapper.
 *
 * @param mapper Mapper a usar para la conversión inicial
 * @param additionalValidation Validación adicional a aplicar después de la conversión
 * @return Result con el dominio validado o error
 */
public inline fun <DTO, Domain> DTO.toDomainWithValidation(
    mapper: DomainMapper<DTO, Domain>,
    crossinline additionalValidation: (Domain) -> Result<Domain>
): Result<Domain> {
    return mapper.toDomain(this).flatMap { domain ->
        additionalValidation(domain)
    }
}

/**
 * Convierte un DTO a dominio con retry en caso de error específico.
 *
 * Útil para casos donde se puede intentar una conversión alternativa si falla.
 *
 * @param primaryMapper Mapper principal a intentar primero
 * @param fallbackMapper Mapper alternativo si el primero falla
 * @return Result del mapper principal, o del fallback si el primero falla
 */
public fun <DTO, Domain> DTO.toDomainWithFallback(
    primaryMapper: DomainMapper<DTO, Domain>,
    fallbackMapper: DomainMapper<DTO, Domain>
): Result<Domain> {
    return when (val primaryResult = primaryMapper.toDomain(this)) {
        is Result.Success -> primaryResult
        is Result.Failure -> fallbackMapper.toDomain(this)
        is Result.Loading -> Result.Loading
    }
}

/**
 * Batch conversion con procesamiento parcial.
 *
 * Convierte una lista de DTOs a dominios, pero continúa procesando incluso
 * si algunos fallan. Retorna los éxitos y los errores por separado.
 *
 * @param mapper Mapper a usar para cada conversión
 * @return Par con lista de éxitos y lista de errores (índice + mensaje)
 */
public fun <DTO, Domain> List<DTO>.toDomainPartial(
    mapper: DomainMapper<DTO, Domain>
): Pair<List<Domain>, List<Pair<Int, String>>> {
    val successes = mutableListOf<Domain>()
    val failures = mutableListOf<Pair<Int, String>>()

    forEachIndexed { index, dto ->
        when (val result = mapper.toDomain(dto)) {
            is Result.Success -> successes.add(result.data)
            is Result.Failure -> failures.add(index to result.error)
            is Result.Loading -> { /* Ignorar loading en batch */ }
        }
    }

    return successes to failures
}

/**
 * Convierte DTOs a dominios y filtra solo los exitosos.
 *
 * Similar a toDomainPartial pero solo retorna los éxitos, descartando
 * silenciosamente los errores.
 *
 * @param mapper Mapper a usar para cada conversión
 * @return Lista de dominios convertidos exitosamente
 */
public fun <DTO, Domain> List<DTO>.toDomainListIgnoreErrors(
    mapper: DomainMapper<DTO, Domain>
): List<Domain> {
    return mapNotNull { dto ->
        when (val result = mapper.toDomain(dto)) {
            is Result.Success -> result.data
            is Result.Failure -> null
            is Result.Loading -> null
        }
    }
}

/**
 * Convierte una lista de DTOs con métricas de conversión.
 *
 * Retorna el resultado junto con estadísticas de la conversión.
 *
 * @param mapper Mapper a usar para cada conversión
 * @return Par con el resultado y las métricas de conversión
 */
public fun <DTO, Domain> List<DTO>.toDomainListWithMetrics(
    mapper: DomainMapper<DTO, Domain>
): Pair<Result<List<Domain>>, ConversionMetrics> {
    val (partialResult, duration) = measureTimedValue {
        toDomainPartial(mapper)
    }
    val (successes, failures) = partialResult
    val durationMs = duration.inWholeMilliseconds

    val metrics = ConversionMetrics(
        total = size,
        successCount = successes.size,
        failureCount = failures.size,
        durationMs = durationMs,
        failures = failures
    )

    val result = if (failures.isEmpty()) {
        Result.Success(successes)
    } else {
        Result.Failure("Conversion failed for ${failures.size} items: ${failures.joinToString("; ") { "${it.first}: ${it.second}" }}")
    }

    return result to metrics
}

/**
 * Métricas de conversión batch.
 *
 * Contiene estadísticas sobre una operación de conversión masiva.
 *
 * @property total Número total de items procesados
 * @property successCount Número de conversiones exitosas
 * @property failureCount Número de conversiones fallidas
 * @property durationMs Duración de la conversión en milisegundos
 * @property failures Lista de índices y mensajes de error de los items que fallaron
 */
public data class ConversionMetrics(
    val total: Int,
    val successCount: Int,
    val failureCount: Int,
    val durationMs: Long,
    val failures: List<Pair<Int, String>>
) {
    /**
     * Tasa de éxito como porcentaje (0.0 a 100.0).
     */
    val successRate: Double
        get() = if (total == 0) 0.0 else (successCount.toDouble() / total) * 100.0

    /**
     * Indica si la conversión fue 100% exitosa.
     */
    val isFullSuccess: Boolean
        get() = failureCount == 0

    /**
     * Indica si hubo al menos un éxito.
     */
    val hasAnySuccess: Boolean
        get() = successCount > 0
}

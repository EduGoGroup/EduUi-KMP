package com.edugo.kmp.foundation.mapper

import com.edugo.kmp.foundation.error.ErrorCode
import com.edugo.kmp.foundation.result.Result
import com.edugo.kmp.foundation.result.success
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

// ========== Test Helpers ==========
// Defined locally to avoid dependency on validation module (Task 1.6)

data class UserDto(
    val email: String,
    val age: Int,
    val username: String
)

data class User(
    val email: String,
    val age: Int,
    val username: String
) {
    companion object {
        const val MIN_AGE = 18
        const val MAX_AGE = 120
        const val USERNAME_MIN_LENGTH = 3
        const val USERNAME_MAX_LENGTH = 30
    }
}

object UserMapper : DomainMapper<UserDto, User> {
    override fun toDomain(dto: UserDto): Result<User> {
        val errors = mutableListOf<String>()

        // Validate email
        if (!dto.email.contains("@") || dto.email.startsWith("@") || dto.email.endsWith("@")) {
            errors.add("Invalid email format")
        } else {
            val parts = dto.email.split("@")
            if (parts.size != 2 || parts[0].isEmpty() || parts[1].isEmpty()) {
                errors.add("Invalid email format")
            }
        }

        // Validate age
        if (dto.age < User.MIN_AGE || dto.age > User.MAX_AGE) {
            errors.add("Age must be between ${User.MIN_AGE} and ${User.MAX_AGE}")
        }

        // Validate username length
        if (dto.username.length < User.USERNAME_MIN_LENGTH || dto.username.length > User.USERNAME_MAX_LENGTH) {
            errors.add("Username must be between ${User.USERNAME_MIN_LENGTH} and ${User.USERNAME_MAX_LENGTH} characters")
        }

        return if (errors.isEmpty()) {
            success(User(
                email = dto.email,
                age = dto.age,
                username = dto.username
            ))
        } else {
            Result.Failure(errors.joinToString("; "))
        }
    }

    override fun toDto(domain: User): UserDto {
        return UserDto(
            email = domain.email,
            age = domain.age,
            username = domain.username
        )
    }
}

/**
 * Suite de tests para DomainMapper y sus extensiones.
 *
 * Verifica:
 * - Conversión DTO -> Domain con validación
 * - Conversión Domain -> DTO sin validación
 * - Extension functions (toDomain, toDto, toDomainList, toDtoList)
 * - Integración con AppError
 * - Conversiones batch (listas)
 * - MapperExtensions avanzadas
 * - Ejemplo UserMapper con validación acumulativa
 */
class DomainMapperTest {

    // ========== Tests de UserMapper - Conversiones Básicas ==========

    @Test
    fun toDomain_convierte_DTO_valido_a_User_exitosamente() {
        val dto = UserDto(
            email = "test@example.com",
            age = 25,
            username = "testuser"
        )

        val result = UserMapper.toDomain(dto)

        assertIs<Result.Success<User>>(result)
        assertEquals("test@example.com", result.data.email)
        assertEquals(25, result.data.age)
        assertEquals("testuser", result.data.username)
    }

    @Test
    fun toDomain_falla_con_email_invalido() {
        val dto = UserDto(
            email = "invalid-email",
            age = 25,
            username = "testuser"
        )

        val result = UserMapper.toDomain(dto)

        assertIs<Result.Failure>(result)
        assertTrue(result.error.contains("Invalid email format"))
    }

    @Test
    fun toDomain_falla_con_edad_menor_al_minimo() {
        val dto = UserDto(
            email = "test@example.com",
            age = 15,
            username = "testuser"
        )

        val result = UserMapper.toDomain(dto)

        assertIs<Result.Failure>(result)
        assertTrue(result.error.contains("Age must be between 18 and 120"))
    }

    @Test
    fun toDomain_falla_con_edad_mayor_al_maximo() {
        val dto = UserDto(
            email = "test@example.com",
            age = 150,
            username = "testuser"
        )

        val result = UserMapper.toDomain(dto)

        assertIs<Result.Failure>(result)
        assertTrue(result.error.contains("Age must be between 18 and 120"))
    }

    @Test
    fun toDomain_falla_con_username_muy_corto() {
        val dto = UserDto(
            email = "test@example.com",
            age = 25,
            username = "ab"
        )

        val result = UserMapper.toDomain(dto)

        assertIs<Result.Failure>(result)
        assertTrue(result.error.contains("Username must be between 3 and 30 characters"))
    }

    @Test
    fun toDomain_falla_con_username_muy_largo() {
        val dto = UserDto(
            email = "test@example.com",
            age = 25,
            username = "a".repeat(31)
        )

        val result = UserMapper.toDomain(dto)

        assertIs<Result.Failure>(result)
        assertTrue(result.error.contains("Username must be between 3 and 30 characters"))
    }

    @Test
    fun toDomain_con_multiples_errores_acumula_todos() {
        val dto = UserDto(
            email = "no-at-sign",
            age = 15,
            username = "ab"
        )

        val result = UserMapper.toDomain(dto)

        assertIs<Result.Failure>(result)
        assertTrue(result.error.contains("Invalid email format"))
        assertTrue(result.error.contains("Age must be between 18 and 120"))
        assertTrue(result.error.contains("Username must be between 3 and 30 characters"))
    }

    @Test
    fun toDomain_acepta_edad_en_limite_inferior() {
        val dto = UserDto(
            email = "test@example.com",
            age = 18,
            username = "testuser"
        )

        val result = UserMapper.toDomain(dto)
        assertIs<Result.Success<User>>(result)
    }

    @Test
    fun toDomain_acepta_edad_en_limite_superior() {
        val dto = UserDto(
            email = "test@example.com",
            age = 120,
            username = "testuser"
        )

        val result = UserMapper.toDomain(dto)
        assertIs<Result.Success<User>>(result)
    }

    @Test
    fun toDomain_acepta_username_en_limite_inferior() {
        val dto = UserDto(
            email = "test@example.com",
            age = 25,
            username = "abc"
        )

        val result = UserMapper.toDomain(dto)
        assertIs<Result.Success<User>>(result)
    }

    @Test
    fun toDomain_acepta_username_en_limite_superior() {
        val dto = UserDto(
            email = "test@example.com",
            age = 25,
            username = "a".repeat(30)
        )

        val result = UserMapper.toDomain(dto)
        assertIs<Result.Success<User>>(result)
    }

    @Test
    fun toDto_convierte_User_a_DTO_sin_validacion() {
        val user = User(
            email = "test@example.com",
            age = 25,
            username = "testuser"
        )

        val dto = UserMapper.toDto(user)

        assertEquals("test@example.com", dto.email)
        assertEquals(25, dto.age)
        assertEquals("testuser", dto.username)
    }

    // ========== Tests de Extension Functions ==========

    @Test
    fun extension_toDomain_convierte_DTO_a_dominio() {
        val dto = UserDto(
            email = "test@example.com",
            age = 25,
            username = "testuser"
        )

        val result = dto.toDomain(UserMapper)

        assertIs<Result.Success<User>>(result)
        assertEquals("testuser", result.data.username)
    }

    @Test
    fun extension_toDto_convierte_dominio_a_DTO() {
        val user = User(
            email = "test@example.com",
            age = 25,
            username = "testuser"
        )

        val dto = user.toDto(UserMapper)

        assertEquals("testuser", dto.username)
    }

    @Test
    fun toDomainList_convierte_lista_de_DTOs_exitosamente() {
        val dtos = listOf(
            UserDto("test1@example.com", 25, "user1"),
            UserDto("test2@example.com", 30, "user2"),
            UserDto("test3@example.com", 35, "user3")
        )

        val result = dtos.toDomainList(UserMapper)

        assertIs<Result.Success<List<User>>>(result)
        assertEquals(3, result.data.size)
        assertEquals("user1", result.data[0].username)
        assertEquals("user2", result.data[1].username)
        assertEquals("user3", result.data[2].username)
    }

    @Test
    fun toDomainList_falla_con_el_primer_error() {
        val dtos = listOf(
            UserDto("test1@example.com", 25, "user1"),
            UserDto("invalid", 30, "user2"),
            UserDto("test3@example.com", 35, "user3")
        )

        val result = dtos.toDomainList(UserMapper)

        assertIs<Result.Failure>(result)
        assertTrue(result.error.contains("Invalid email format"))
    }

    @Test
    fun toDomainList_con_lista_vacia_retorna_Success_con_lista_vacia() {
        val dtos = emptyList<UserDto>()
        val result = dtos.toDomainList(UserMapper)

        assertIs<Result.Success<List<User>>>(result)
        assertTrue(result.data.isEmpty())
    }

    @Test
    fun toDtoList_convierte_lista_de_dominios_a_DTOs() {
        val users = listOf(
            User("test1@example.com", 25, "user1"),
            User("test2@example.com", 30, "user2"),
            User("test3@example.com", 35, "user3")
        )

        val dtos = users.toDtoList(UserMapper)

        assertEquals(3, dtos.size)
        assertEquals("user1", dtos[0].username)
        assertEquals("user2", dtos[1].username)
        assertEquals("user3", dtos[2].username)
    }

    // ========== Tests de MapperExtensions con AppError ==========

    @Test
    fun toDomainWithAppError_convierte_exitosamente() {
        val dto = UserDto("test@example.com", 25, "testuser")
        val result = dto.toDomainWithAppError(UserMapper)

        assertIs<Result.Success<User>>(result)
    }

    @Test
    fun toDomainWithAppError_falla_con_AppError_estructurado() {
        val dto = UserDto("invalid", 25, "testuser")
        val result = dto.toDomainWithAppError(
            mapper = UserMapper,
            errorCode = ErrorCode.VALIDATION_INVALID_EMAIL,
            details = mapOf("field" to "email", "value" to "invalid")
        )

        assertIs<Result.Failure>(result)
        assertTrue(result.error.contains("VALIDATION_INVALID_EMAIL"))
    }

    @Test
    fun toDomainListWithAppError_convierte_lista_exitosamente() {
        val dtos = listOf(
            UserDto("test1@example.com", 25, "user1"),
            UserDto("test2@example.com", 30, "user2")
        )

        val result = dtos.toDomainListWithAppError(UserMapper)

        assertIs<Result.Success<List<User>>>(result)
        assertEquals(2, result.data.size)
    }

    @Test
    fun toDomainListWithAppError_falla_con_indice_del_item() {
        val dtos = listOf(
            UserDto("test1@example.com", 25, "user1"),
            UserDto("invalid", 30, "user2"),
            UserDto("test3@example.com", 35, "user3")
        )

        val result = dtos.toDomainListWithAppError(UserMapper, includeIndex = true)

        assertIs<Result.Failure>(result)
        assertTrue(result.error.contains("VALIDATION_INVALID_INPUT"))
    }

    // ========== Tests de MapperExtensions Avanzadas ==========

    @Test
    fun mapDomain_transforma_el_resultado_exitosamente() {
        val dto = UserDto("test@example.com", 25, "testuser")
        val result = dto.toDomain(UserMapper)
            .mapDomain { user -> user.username.uppercase() }

        assertIs<Result.Success<String>>(result)
        assertEquals("TESTUSER", result.data)
    }

    @Test
    fun mapDomain_propaga_el_error() {
        val dto = UserDto("invalid", 25, "testuser")
        val result = dto.toDomain(UserMapper)
            .mapDomain { user -> user.username.uppercase() }

        assertIs<Result.Failure>(result)
    }

    @Test
    fun flatMapDomain_encadena_operaciones_exitosamente() {
        val dto = UserDto("test@example.com", 25, "testuser")
        val result = dto.toDomain(UserMapper)
            .flatMapDomain { user ->
                if (user.age >= 18) {
                    success("Adult: ${user.username}")
                } else {
                    Result.Failure("Not an adult")
                }
            }

        assertIs<Result.Success<String>>(result)
        assertEquals("Adult: testuser", result.data)
    }

    @Test
    fun flatMapDomain_propaga_error_de_validacion() {
        val dto = UserDto("invalid", 25, "testuser")
        val result = dto.toDomain(UserMapper)
            .flatMapDomain { user ->
                success(user.username)
            }

        assertIs<Result.Failure>(result)
    }

    @Test
    fun flatMapDomain_puede_fallar_en_la_transformacion() {
        val dto = UserDto("test@example.com", 25, "testuser")
        val result = dto.toDomain(UserMapper)
            .flatMapDomain { user ->
                Result.Failure("Something went wrong in transformation")
            }

        assertIs<Result.Failure>(result)
        assertEquals("Something went wrong in transformation", result.error)
    }

    @Test
    fun toDomainWithValidation_aplica_validacion_adicional() {
        val dto = UserDto("test@example.com", 25, "testuser")
        val result = dto.toDomainWithValidation(UserMapper) { user ->
            if (user.username.startsWith("test")) {
                Result.Failure("Username cannot start with 'test'")
            } else {
                success(user)
            }
        }

        assertIs<Result.Failure>(result)
        assertEquals("Username cannot start with 'test'", result.error)
    }

    @Test
    fun toDomainWithValidation_pasa_si_validacion_adicional_es_exitosa() {
        val dto = UserDto("test@example.com", 25, "validuser")
        val result = dto.toDomainWithValidation(UserMapper) { user ->
            if (user.username.startsWith("test")) {
                Result.Failure("Username cannot start with 'test'")
            } else {
                success(user)
            }
        }

        assertIs<Result.Success<User>>(result)
        assertEquals("validuser", result.data.username)
    }

    @Test
    fun toDomainWithFallback_usa_mapper_primario_si_tiene_exito() {
        val dto = UserDto("test@example.com", 25, "testuser")
        val result = dto.toDomainWithFallback(
            primaryMapper = UserMapper,
            fallbackMapper = UserMapper
        )

        assertIs<Result.Success<User>>(result)
    }

    @Test
    fun toDomainWithFallback_usa_fallback_si_primario_falla() {
        val lenientMapper = object : DomainMapper<UserDto, User> {
            override fun toDomain(dto: UserDto): Result<User> {
                return success(User(
                    email = dto.email.ifBlank { "default@example.com" },
                    age = dto.age.coerceIn(18, 120),
                    username = dto.username.ifBlank { "default" }
                ))
            }
            override fun toDto(domain: User) = UserMapper.toDto(domain)
        }

        val dto = UserDto("invalid", 15, "ab")
        val result = dto.toDomainWithFallback(
            primaryMapper = UserMapper,
            fallbackMapper = lenientMapper
        )

        assertIs<Result.Success<User>>(result)
    }

    // ========== Tests de Conversión Parcial ==========

    @Test
    fun toDomainPartial_procesa_todos_los_items() {
        val dtos = listOf(
            UserDto("test1@example.com", 25, "user1"),
            UserDto("invalid", 30, "user2"),
            UserDto("test3@example.com", 35, "user3")
        )

        val (successes, failures) = dtos.toDomainPartial(UserMapper)

        assertEquals(2, successes.size)
        assertEquals(1, failures.size)
        assertEquals(1, failures[0].first) // Índice 1 falló
        assertTrue(failures[0].second.contains("Invalid email format"))
    }

    @Test
    fun toDomainListIgnoreErrors_filtra_solo_exitos() {
        val dtos = listOf(
            UserDto("test1@example.com", 25, "user1"),
            UserDto("invalid", 30, "user2"),
            UserDto("test3@example.com", 35, "user3")
        )

        val users = dtos.toDomainListIgnoreErrors(UserMapper)

        assertEquals(2, users.size)
        assertEquals("user1", users[0].username)
        assertEquals("user3", users[1].username)
    }

    @Test
    fun toDomainListIgnoreErrors_retorna_lista_vacia_si_todos_fallan() {
        val dtos = listOf(
            UserDto("invalid1", 15, "ab"),
            UserDto("invalid2", 200, "cd")
        )

        val users = dtos.toDomainListIgnoreErrors(UserMapper)

        assertTrue(users.isEmpty())
    }

    // ========== Tests de Métricas ==========

    @Test
    fun toDomainListWithMetrics_retorna_metricas_correctas_para_conversion_exitosa() {
        val dtos = listOf(
            UserDto("test1@example.com", 25, "user1"),
            UserDto("test2@example.com", 30, "user2"),
            UserDto("test3@example.com", 35, "user3")
        )

        val (result, metrics) = dtos.toDomainListWithMetrics(UserMapper)

        assertIs<Result.Success<List<User>>>(result)
        assertEquals(3, metrics.total)
        assertEquals(3, metrics.successCount)
        assertEquals(0, metrics.failureCount)
        assertEquals(100.0, metrics.successRate)
        assertTrue(metrics.isFullSuccess)
        assertTrue(metrics.hasAnySuccess)
    }

    @Test
    fun toDomainListWithMetrics_retorna_metricas_correctas_con_fallos() {
        val dtos = listOf(
            UserDto("test1@example.com", 25, "user1"),
            UserDto("invalid", 30, "user2"),
            UserDto("test3@example.com", 35, "user3")
        )

        val (result, metrics) = dtos.toDomainListWithMetrics(UserMapper)

        assertIs<Result.Failure>(result)
        assertEquals(3, metrics.total)
        assertEquals(2, metrics.successCount)
        assertEquals(1, metrics.failureCount)
        assertTrue(kotlin.math.abs(66.667 - metrics.successRate) < 0.1)
        assertTrue(!metrics.isFullSuccess)
        assertTrue(metrics.hasAnySuccess)
    }

    @Test
    fun toDomainListWithMetrics_retorna_metricas_para_lista_vacia() {
        val dtos = emptyList<UserDto>()
        val (result, metrics) = dtos.toDomainListWithMetrics(UserMapper)

        assertIs<Result.Success<List<User>>>(result)
        assertEquals(0, metrics.total)
        assertEquals(0, metrics.successCount)
        assertEquals(0, metrics.failureCount)
        assertEquals(0.0, metrics.successRate)
        assertTrue(metrics.isFullSuccess)
        assertTrue(!metrics.hasAnySuccess)
    }

    // ========== Tests de Round-Trip ==========

    @Test
    fun roundTrip_conversion_mantiene_datos() {
        val originalDto = UserDto("test@example.com", 25, "testuser")

        val userResult = originalDto.toDomain(UserMapper)
        assertIs<Result.Success<User>>(userResult)

        val finalDto = userResult.data.toDto(UserMapper)

        assertEquals(originalDto, finalDto)
    }

    @Test
    fun multiples_roundTrips_mantienen_datos() {
        val dto1 = UserDto("test@example.com", 25, "testuser")

        val user1 = (dto1.toDomain(UserMapper) as Result.Success).data
        val dto2 = user1.toDto(UserMapper)
        val user2 = (dto2.toDomain(UserMapper) as Result.Success).data
        val dto3 = user2.toDto(UserMapper)

        assertEquals(dto1, dto2)
        assertEquals(dto2, dto3)
    }

    // ========== Tests de Casos Edge ==========

    @Test
    fun toDomain_con_email_con_multiples_at() {
        val dto = UserDto("test@@example.com", 25, "testuser")
        val result = UserMapper.toDomain(dto)

        assertIs<Result.Failure>(result)
        assertTrue(result.error.contains("Invalid email format"))
    }

    @Test
    fun toDomain_con_email_sin_dominio() {
        val dto = UserDto("test@", 25, "testuser")
        val result = UserMapper.toDomain(dto)

        assertIs<Result.Failure>(result)
    }

    @Test
    fun toDomain_con_email_sin_local_part() {
        val dto = UserDto("@example.com", 25, "testuser")
        val result = UserMapper.toDomain(dto)

        assertIs<Result.Failure>(result)
    }

    @Test
    fun toDomain_con_username_con_espacios() {
        val dto = UserDto("test@example.com", 25, "user name")
        val result = UserMapper.toDomain(dto)

        // Debería pasar - solo validamos longitud, no contenido
        assertIs<Result.Success<User>>(result)
    }

    @Test
    fun toDomain_con_username_con_caracteres_especiales() {
        val dto = UserDto("test@example.com", 25, "user_123")
        val result = UserMapper.toDomain(dto)

        assertIs<Result.Success<User>>(result)
    }

    @Test
    fun performance_test_conversion_de_1000_DTOs_validos() {
        val dtos = (1..1000).map { i ->
            UserDto("test$i@example.com", 25, "user$i")
        }

        val (result, metrics) = dtos.toDomainListWithMetrics(UserMapper)

        assertIs<Result.Success<List<User>>>(result)
        assertEquals(1000, result.data.size)
        assertEquals(1000, metrics.successCount)
        assertTrue(metrics.durationMs < 5000) // Debería ser rápido
    }
}

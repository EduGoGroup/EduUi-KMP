package com.edugo.kmp.auth.repository

import com.edugo.kmp.auth.model.AuthUserInfo
import com.edugo.kmp.auth.model.LoginCredentials
import com.edugo.kmp.auth.model.LoginResponse
import com.edugo.kmp.foundation.error.ErrorCode
import com.edugo.kmp.foundation.result.Result
import kotlinx.coroutines.delay

/**
 * Implementacion stub del repositorio de autenticacion para testing.
 *
 * Esta implementacion simula las respuestas del backend sin realizar llamadas
 * de red reales. Es util para:
 * - Tests unitarios que no requieren red
 * - Desarrollo offline
 * - Prototipado rapido de UI
 * - Tests de integracion con comportamiento controlado
 *
 * ## Comportamiento por Defecto
 *
 * - **Login exitoso**: `test@edugo.com` / `password123`
 * - **Login fallido**: Cualquier otra combinacion
 * - **Logout**: Siempre exitoso
 * - **Refresh**: Siempre exitoso con tokens nuevos
 *
 * ## Configuracion de Comportamiento
 *
 * Puedes controlar el comportamiento del stub mediante propiedades:
 *
 * ```kotlin
 * val stub = StubAuthRepository()
 *
 * // Simular error de red
 * stub.simulateNetworkError = true
 * val result = stub.login(credentials) // Result.Failure(network error)
 *
 * // Simular delay (latencia)
 * stub.simulateDelay = 1000 // 1 segundo
 * val result = stub.login(credentials) // Espera 1 segundo antes de responder
 *
 * // Restaurar comportamiento normal
 * stub.simulateNetworkError = false
 * stub.simulateDelay = 0
 * ```
 *
 * ## Credenciales de Prueba
 *
 * ```kotlin
 * // Login exitoso
 * val validCredentials = LoginCredentials(
 *     email = "test@edugo.com",
 *     password = "password123"
 * )
 *
 * // Login fallido
 * val invalidCredentials = LoginCredentials(
 *     email = "wrong@edugo.com",
 *     password = "wrongpass"
 * )
 * ```
 */
public class StubAuthRepository : AuthRepository {

    /**
     * Si es true, todas las operaciones retornan error de red.
     */
    public var simulateNetworkError: Boolean = false

    /**
     * Delay en milisegundos para simular latencia de red.
     * Default: 0 (sin delay)
     */
    public var simulateDelay: Long = 0

    /**
     * Email valido para login exitoso.
     * Cualquier otro email fallara.
     */
    public var validEmail: String = "test@edugo.com"

    /**
     * Password valido para login exitoso.
     * Cualquier otro password fallara.
     */
    public var validPassword: String = "password123"

    /**
     * Usuario de prueba que se retorna en login exitoso.
     * Puede ser modificado para personalizar los tests.
     */
    public var testUser: AuthUserInfo = AuthUserInfo.createTestUser(
        id = "stub-user-123",
        email = "test@edugo.com",
        firstName = "Test",
        lastName = "User",
        role = "student"
    )

    override suspend fun login(credentials: LoginCredentials): Result<LoginResponse> {
        // Simular delay si esta configurado
        if (simulateDelay > 0) {
            delay(simulateDelay)
        }

        // Simular error de red si esta configurado
        if (simulateNetworkError) {
            return Result.Failure(ErrorCode.NETWORK_TIMEOUT.description)
        }

        // Validar credenciales
        return if (credentials.email == validEmail && credentials.password == validPassword) {
            // Login exitoso
            Result.Success(
                LoginResponse.createTestResponse(
                    user = testUser
                )
            )
        } else {
            // Credenciales invalidas
            Result.Failure(ErrorCode.AUTH_INVALID_CREDENTIALS.description)
        }
    }

    override suspend fun logout(accessToken: String): Result<Unit> {
        // Simular delay si esta configurado
        if (simulateDelay > 0) {
            delay(simulateDelay)
        }

        // Simular error de red si esta configurado
        if (simulateNetworkError) {
            return Result.Failure(ErrorCode.NETWORK_TIMEOUT.description)
        }

        // Logout siempre exitoso en stub
        return Result.Success(Unit)
    }

    override suspend fun refresh(refreshToken: String): Result<RefreshResponse> {
        // Simular delay si esta configurado
        if (simulateDelay > 0) {
            delay(simulateDelay)
        }

        // Simular error de red si esta configurado
        if (simulateNetworkError) {
            return Result.Failure(ErrorCode.NETWORK_TIMEOUT.description)
        }

        // Validar que el refresh token no este vacio
        return if (refreshToken.isNotBlank()) {
            // Refresh exitoso con nuevo access token
            Result.Success(RefreshResponse.createTestResponse())
        } else {
            // Refresh token invalido
            Result.Failure(ErrorCode.AUTH_REFRESH_TOKEN_INVALID.description)
        }
    }

    /**
     * Configuracion para tests de verifyToken.
     */
    public var verifyTokenShouldSucceed: Boolean = true

    /**
     * Respuesta personalizada para verifyToken (null = usar default).
     */
    public var verifyTokenResponse: TokenVerificationResponse? = null

    override suspend fun verifyToken(token: String): Result<TokenVerificationResponse> {
        // Simular delay si esta configurado
        if (simulateDelay > 0) {
            delay(simulateDelay)
        }

        // Simular error de red si esta configurado
        if (simulateNetworkError) {
            return Result.Failure(ErrorCode.NETWORK_TIMEOUT.description)
        }

        // Si no se debe tener exito, retornar error
        if (!verifyTokenShouldSucceed) {
            return Result.Failure("Verification failed")
        }

        // Respuesta por defecto si no se configura una especifica
        val response = verifyTokenResponse ?: TokenVerificationResponse(
            valid = true,
            userId = "test-user-id",
            email = "test@edugo.com",
            role = "user",
            schoolId = "test-school",
            expiresAt = kotlinx.datetime.Clock.System.now().plus(kotlin.time.Duration.parse("1h")).toString(),
            error = null
        )

        return Result.Success(response)
    }

    /**
     * Resetea todas las configuraciones a sus valores por defecto.
     *
     * Util para limpiar estado entre tests.
     *
     * ```kotlin
     * @AfterTest
     * fun cleanup() {
     *     (repository as StubAuthRepository).reset()
     * }
     * ```
     */
    public fun reset() {
        simulateNetworkError = false
        simulateDelay = 0
        validEmail = "test@edugo.com"
        validPassword = "password123"
        testUser = AuthUserInfo.createTestUser(
            id = "stub-user-123",
            email = "test@edugo.com",
            firstName = "Test",
            lastName = "User",
            role = "student"
        )
        verifyTokenShouldSucceed = true
        verifyTokenResponse = null
    }

    companion object {
        /**
         * Credenciales validas por defecto.
         */
        public val VALID_CREDENTIALS: LoginCredentials = LoginCredentials(
            email = "test@edugo.com",
            password = "password123"
        )

        /**
         * Credenciales invalidas para testing.
         */
        public val INVALID_CREDENTIALS: LoginCredentials = LoginCredentials(
            email = "invalid@edugo.com",
            password = "wrongpassword"
        )

        /**
         * Refresh token valido para testing.
         */
        public const val VALID_REFRESH_TOKEN: String = "valid_refresh_token_123"

        /**
         * Refresh token invalido para testing.
         */
        public const val INVALID_REFRESH_TOKEN: String = ""

        /**
         * Access token valido para testing.
         */
        public const val VALID_ACCESS_TOKEN: String = "valid_access_token_123"

        /**
         * Crea instancia con configuracion por defecto.
         */
        public fun create(): StubAuthRepository = StubAuthRepository()

        /**
         * Crea instancia que siempre retorna error de red.
         *
         * Util para tests de manejo de errores de conectividad.
         */
        public fun createWithNetworkError(): StubAuthRepository {
            return StubAuthRepository().apply {
                simulateNetworkError = true
            }
        }

        /**
         * Crea instancia con delay especifico.
         *
         * Util para tests de timeout o loading states.
         */
        public fun createWithDelay(delayMillis: Long): StubAuthRepository {
            return StubAuthRepository().apply {
                simulateDelay = delayMillis
            }
        }

        /**
         * Crea instancia con usuario personalizado.
         *
         * Util para tests que necesitan datos especificos del usuario.
         */
        public fun createWithUser(user: AuthUserInfo): StubAuthRepository {
            return StubAuthRepository().apply {
                testUser = user
                validEmail = user.email
            }
        }
    }
}

package com.edugo.kmp.auth.repository

import com.edugo.kmp.auth.circuit.CircuitBreaker
import com.edugo.kmp.auth.model.LoginCredentials
import com.edugo.kmp.auth.model.LoginResponse
import com.edugo.kmp.foundation.error.ErrorCode
import com.edugo.kmp.foundation.result.Result
import com.edugo.kmp.network.EduGoHttpClient
import com.edugo.kmp.network.ExceptionMapper
import com.edugo.kmp.network.HttpRequestConfig
import io.ktor.client.plugins.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Implementacion del repositorio de autenticacion usando EduGoHttpClient.
 *
 * Esta implementacion realiza llamadas HTTP reales al backend de autenticacion
 * usando Ktor Client envuelto en [EduGoHttpClient]. Maneja automaticamente
 * la serializacion/deserializacion JSON y el mapeo de errores HTTP a Result.
 *
 * ## Arquitectura
 *
 * ```
 * AuthService
 *     |
 * AuthRepositoryImpl (esta clase)
 *     |
 * EduGoHttpClient
 *     |
 * Ktor HttpClient
 *     |
 * Backend API (edu-admin)
 * ```
 *
 * ## Endpoints del Backend
 *
 * - `POST {baseUrl}/v1/auth/login` - Autenticacion
 * - `POST {baseUrl}/v1/auth/logout` - Cerrar sesion
 * - `POST {baseUrl}/v1/auth/refresh` - Renovar token
 * - `POST {baseUrl}/v1/auth/verify` - Verificar token
 *
 * ## Manejo de Errores
 *
 * Los errores HTTP se mapean automaticamente a Result.Failure:
 *
 * | HTTP Status | ErrorCode | Descripcion |
 * |-------------|-----------|-------------|
 * | 400 | VALIDATION_INVALID_INPUT | Datos invalidos |
 * | 401 | AUTH_INVALID_CREDENTIALS | Credenciales incorrectas |
 * | 403 | AUTH_FORBIDDEN | Usuario inactivo/sin permisos |
 * | 404 | BUSINESS_RESOURCE_NOT_FOUND | Usuario no encontrado |
 * | 423 | AUTH_ACCOUNT_LOCKED | Cuenta bloqueada |
 * | 500 | SYSTEM_INTERNAL_ERROR | Error del servidor |
 * | 502 | NETWORK_SERVER_ERROR | Backend no disponible |
 * | 503 | SYSTEM_SERVICE_UNAVAILABLE | Servicio temporalmente no disponible |
 *
 * ## Testing
 *
 * Para testing, usar [StubAuthRepository] en lugar de esta implementacion
 * para evitar llamadas de red reales.
 *
 * @property httpClient Cliente HTTP configurado (EduGoHttpClient)
 * @property baseUrl URL base del backend (ej: "https://api.edugo.com" o "http://localhost:8081")
 */
public class AuthRepositoryImpl(
    private val httpClient: EduGoHttpClient,
    private val baseUrl: String,
    private val circuitBreaker: CircuitBreaker = CircuitBreaker()
) : AuthRepository {

    /**
     * Request body para el endpoint de refresh.
     */
    @Serializable
    private data class RefreshRequest(
        @SerialName("refresh_token")
        val refreshToken: String
    )

    override suspend fun login(credentials: LoginCredentials): Result<LoginResponse> {
        return circuitBreaker.execute {
            performLogin(credentials)
        }
    }

    private suspend fun performLogin(credentials: LoginCredentials): Result<LoginResponse> {
        return try {
            val url = "$baseUrl/v1/auth/login"

            val result = httpClient.postSafe<LoginCredentials, LoginResponse>(
                url = url,
                body = credentials
            )

            when (result) {
                is Result.Success -> result
                is Result.Failure -> result
                is Result.Loading -> Result.Failure("Unexpected loading state")
            }
        } catch (e: ClientRequestException) {
            val errorMessage = when (e.response.status.value) {
                400 -> ErrorCode.VALIDATION_INVALID_INPUT.description
                401 -> ErrorCode.AUTH_INVALID_CREDENTIALS.description
                403 -> ErrorCode.AUTH_FORBIDDEN.description
                404 -> ErrorCode.BUSINESS_RESOURCE_NOT_FOUND.description
                423 -> ErrorCode.AUTH_ACCOUNT_LOCKED.description
                else -> e.message ?: "Request failed"
            }
            Result.Failure(errorMessage)
        } catch (e: ServerResponseException) {
            val errorMessage = when (e.response.status.value) {
                500 -> ErrorCode.SYSTEM_INTERNAL_ERROR.description
                502 -> ErrorCode.NETWORK_SERVER_ERROR.description
                503 -> ErrorCode.SYSTEM_SERVICE_UNAVAILABLE.description
                else -> e.message ?: "Server error"
            }
            Result.Failure(errorMessage)
        } catch (e: Throwable) {
            val networkException = ExceptionMapper.map(e)
            Result.Failure(networkException.toAppError().toString())
        }
    }

    override suspend fun logout(accessToken: String): Result<Unit> {
        return try {
            val url = "$baseUrl/v1/auth/logout"

            // Configurar header de autorizacion
            val config = HttpRequestConfig.builder()
                .header("Authorization", "Bearer $accessToken")
                .build()

            // POST sin body y sin respuesta esperada
            httpClient.postNoResponse(url, Unit, config)

            Result.Success(Unit)
        } catch (e: ClientRequestException) {
            // En logout, tipicamente ignoramos errores 401 (token ya invalido)
            if (e.response.status.value == 401) {
                // Token ya invalido, considerar como exito
                Result.Success(Unit)
            } else {
                val errorMessage = when (e.response.status.value) {
                    500 -> ErrorCode.SYSTEM_INTERNAL_ERROR.description
                    else -> e.message ?: "Logout failed"
                }
                Result.Failure(errorMessage)
            }
        } catch (e: ServerResponseException) {
            // Errores del servidor en logout no son criticos
            val errorMessage = ErrorCode.SYSTEM_INTERNAL_ERROR.description
            Result.Failure(errorMessage)
        } catch (e: Throwable) {
            // Errores de red en logout no son criticos
            val networkException = ExceptionMapper.map(e)
            Result.Failure(networkException.toAppError().toString())
        }
    }

    override suspend fun refresh(refreshToken: String): Result<RefreshResponse> {
        return circuitBreaker.execute {
            performRefresh(refreshToken)
        }
    }

    private suspend fun performRefresh(refreshToken: String): Result<RefreshResponse> {
        return try {
            val url = "$baseUrl/v1/auth/refresh"

            val requestBody = RefreshRequest(refreshToken = refreshToken)

            val result = httpClient.postSafe<RefreshRequest, RefreshResponse>(
                url = url,
                body = requestBody
            )

            when (result) {
                is Result.Success -> result
                is Result.Failure -> result
                is Result.Loading -> Result.Failure("Unexpected loading state")
            }
        } catch (e: ClientRequestException) {
            val errorMessage = when (e.response.status.value) {
                401 -> ErrorCode.AUTH_REFRESH_TOKEN_INVALID.description
                403 -> ErrorCode.AUTH_FORBIDDEN.description
                else -> e.message ?: "Refresh failed"
            }
            Result.Failure(errorMessage)
        } catch (e: ServerResponseException) {
            val errorMessage = when (e.response.status.value) {
                500 -> ErrorCode.SYSTEM_INTERNAL_ERROR.description
                502 -> ErrorCode.NETWORK_SERVER_ERROR.description
                503 -> ErrorCode.SYSTEM_SERVICE_UNAVAILABLE.description
                else -> e.message ?: "Server error"
            }
            Result.Failure(errorMessage)
        } catch (e: Throwable) {
            val networkException = ExceptionMapper.map(e)
            Result.Failure(networkException.toAppError().toString())
        }
    }

    override suspend fun verifyToken(token: String): Result<TokenVerificationResponse> {
        return try {
            val url = "$baseUrl/v1/auth/verify"

            // Crear body con el token
            val requestBody = TokenVerificationRequest(token = token)

            // Usar postSafe que retorna Result<T> automaticamente
            val result = httpClient.postSafe<TokenVerificationRequest, TokenVerificationResponse>(
                url = url,
                body = requestBody
            )

            when (result) {
                is Result.Success -> result
                is Result.Failure -> result
                is Result.Loading -> Result.Failure("Unexpected loading state")
            }
        } catch (e: ClientRequestException) {
            // Mapeo explicito de errores HTTP 4xx en verify
            val errorMessage = when (e.response.status.value) {
                400 -> ErrorCode.VALIDATION_INVALID_INPUT.description
                429 -> "Rate limit exceeded"
                else -> e.message ?: "Verification failed"
            }
            Result.Failure(errorMessage)
        } catch (e: ServerResponseException) {
            // Mapeo explicito de errores HTTP 5xx
            val errorMessage = when (e.response.status.value) {
                500 -> ErrorCode.SYSTEM_INTERNAL_ERROR.description
                502 -> ErrorCode.NETWORK_SERVER_ERROR.description
                503 -> ErrorCode.SYSTEM_SERVICE_UNAVAILABLE.description
                else -> e.message ?: "Server error"
            }
            Result.Failure(errorMessage)
        } catch (e: Throwable) {
            // Mapeo de otros errores (network, timeout, etc.)
            val networkException = ExceptionMapper.map(e)
            Result.Failure(networkException.toAppError().toString())
        }
    }

    companion object {
        /**
         * URLs tipicas del backend por entorno.
         */
        public object BaseUrls {
            /** URL para desarrollo local */
            public const val LOCAL: String = "http://localhost:8081"

            /** URL para entorno de desarrollo */
            public const val DEVELOPMENT: String = "https://dev-api.edugo.com"

            /** URL para entorno de staging */
            public const val STAGING: String = "https://staging-api.edugo.com"

            /** URL para produccion */
            public const val PRODUCTION: String = "https://api.edugo.com"
        }

        /**
         * Factory method para crear instancia con configuracion por defecto.
         *
         * @param baseUrl URL base del backend
         * @return Nueva instancia de AuthRepositoryImpl
         */
        public fun create(baseUrl: String = BaseUrls.LOCAL): AuthRepositoryImpl {
            val httpClient = EduGoHttpClient.create()
            return AuthRepositoryImpl(httpClient, baseUrl)
        }

        /**
         * Factory method para testing con HttpClient personalizado.
         *
         * Util para inyectar MockEngine en tests.
         *
         * @param httpClient Cliente HTTP configurado (puede ser mock)
         * @param baseUrl URL base del backend
         * @return Nueva instancia de AuthRepositoryImpl
         */
        public fun withHttpClient(
            httpClient: EduGoHttpClient,
            baseUrl: String = BaseUrls.LOCAL,
            circuitBreaker: CircuitBreaker = CircuitBreaker()
        ): AuthRepositoryImpl {
            return AuthRepositoryImpl(httpClient, baseUrl, circuitBreaker)
        }
    }
}

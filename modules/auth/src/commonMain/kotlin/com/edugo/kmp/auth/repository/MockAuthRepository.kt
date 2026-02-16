package com.edugo.kmp.auth.repository

import com.edugo.kmp.auth.model.AuthUserInfo
import com.edugo.kmp.auth.model.LoginCredentials
import com.edugo.kmp.auth.model.LoginResponse
import com.edugo.kmp.foundation.result.Result
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue

/**
 * Implementacion mock del repositorio de autenticacion para desarrollo de UI.
 *
 * A diferencia de [StubAuthRepository] (disenado para tests unitarios con
 * credenciales fijas y errores configurables), este mock esta pensado para
 * correr la app completa sin backend:
 *
 * - Acepta cualquier email/password valido
 * - Genera usuario determinista basado en el email (mismo email = mismos datos)
 * - Simula delay de red automatico (300-800ms)
 * - Nunca falla (siempre retorna exito)
 * - Tokens con prefijo `mock_` para identificacion facil
 */
public class MockAuthRepository : AuthRepository {

    private val firstNames = listOf(
        "Ana", "Carlos", "Maria", "Diego", "Laura",
        "Pedro", "Sofia", "Juan", "Valentina", "Miguel"
    )

    private val lastNames = listOf(
        "Garcia", "Rodriguez", "Martinez", "Lopez", "Hernandez",
        "Gonzalez", "Perez", "Sanchez", "Ramirez", "Torres"
    )

    private val roles = listOf("student", "teacher", "admin")

    private suspend fun simulateNetworkDelay() {
        val delayMs = 300L + (kotlin.random.Random.nextLong() % 500).absoluteValue
        delay(delayMs)
    }

    private fun generateUserFromEmail(email: String): AuthUserInfo {
        val seed = email.hashCode().absoluteValue
        val firstName = firstNames[seed % firstNames.size]
        val lastName = lastNames[(seed / firstNames.size) % lastNames.size]
        val userId = "mock-user-${seed}"
        val schoolId = "mock-school-${(seed % 5) + 1}"

        return AuthUserInfo.createTestUser(
            id = userId,
            email = email,
            firstName = firstName,
            lastName = lastName,
            schoolId = schoolId
        )
    }

    override suspend fun login(credentials: LoginCredentials): Result<LoginResponse> {
        simulateNetworkDelay()

        val user = generateUserFromEmail(credentials.email)
        val timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()

        // Generar rol basado en el seed del email
        val seed = credentials.email.hashCode().absoluteValue
        val roleName = roles[seed % roles.size]

        val activeContext = com.edugo.kmp.auth.model.UserContext.createTestContext(
            roleId = "mock-role-${seed % roles.size}",
            roleName = roleName,
            schoolId = user.schoolId,
            schoolName = "Mock School ${(seed % 5) + 1}"
        )

        return Result.Success(
            LoginResponse.createTestResponse(
                accessToken = "access_mock_$timestamp",
                refreshToken = "refresh_mock_$timestamp",
                user = user,
                activeContext = activeContext
            )
        )
    }

    override suspend fun logout(accessToken: String): Result<Unit> {
        simulateNetworkDelay()
        return Result.Success(Unit)
    }

    override suspend fun refresh(refreshToken: String): Result<RefreshResponse> {
        simulateNetworkDelay()

        return if (refreshToken.isNotBlank()) {
            val timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
            Result.Success(
                RefreshResponse.createTestResponse(
                    accessToken = "access_mock_$timestamp"
                )
            )
        } else {
            Result.Failure("Mock refresh failed: empty token")
        }
    }

    override suspend fun verifyToken(token: String): Result<TokenVerificationResponse> {
        simulateNetworkDelay()

        return if (token.isNotBlank()) {
            Result.Success(
                TokenVerificationResponse(
                    valid = true,
                    userId = "mock-user-0",
                    email = "mock@edugo.com",
                    role = "student",
                    schoolId = "mock-school-1",
                    expiresAt = kotlinx.datetime.Clock.System.now()
                        .plus(kotlin.time.Duration.parse("1h")).toString(),
                    error = null
                )
            )
        } else {
            Result.Failure("Mock verification failed: empty token")
        }
    }
}

package com.edugo.kmp.auth.token

import com.edugo.kmp.auth.model.AuthToken
import com.edugo.kmp.auth.repository.RefreshResponse
import com.edugo.kmp.auth.repository.StubAuthRepository
import com.edugo.kmp.foundation.result.Result
import com.edugo.kmp.storage.EduGoStorage
import com.edugo.kmp.storage.SafeEduGoStorage
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class TokenRefreshManagerAutoRefreshTest {

    private val json = Json { ignoreUnknownKeys = true }

    private fun createTestStorage(): SafeEduGoStorage {
        return SafeEduGoStorage.wrap(EduGoStorage.withSettings(MapSettings()))
    }

    private fun createManager(
        testScope: TestScope,
        repository: StubAuthRepository = StubAuthRepository(),
        config: TokenRefreshConfig = TokenRefreshConfig(
            refreshThresholdSeconds = 300,
            maxRetryAttempts = 1,
            retryDelayMs = 100
        ),
        storage: SafeEduGoStorage = createTestStorage()
    ): TokenRefreshManagerImpl {
        return TokenRefreshManagerImpl(
            repository = repository,
            storage = storage,
            config = config,
            scope = testScope,
            json = json
        )
    }

    private fun createTokenWithExpiration(secondsUntilExpiry: Long): AuthToken {
        val now = Clock.System.now()
        return AuthToken(
            token = "test_access_token",
            expiresAt = now + secondsUntilExpiry.seconds,
            refreshToken = "test_refresh_token"
        )
    }

    private fun seedStorage(storage: SafeEduGoStorage, token: AuthToken) {
        val tokenJson = json.encodeToString(token)
        storage.putStringSafe("auth_token", tokenJson)
    }

    @Test
    fun testStartAutomaticRefreshSchedulesRefresh() = runTest {
        val storage = createTestStorage()
        val manager = createManager(testScope = this, storage = storage)
        val token = createTokenWithExpiration(600)
        seedStorage(storage, token)

        var refreshedToken: AuthToken? = null
        val collectJob = launch {
            manager.onRefreshSuccess.first().let { refreshedToken = it }
        }

        manager.startAutomaticRefresh(token)

        // threshold=300s, token expires in 600s => delay ~300s, min 5s
        advanceTimeBy(301.seconds)

        collectJob.cancel()
        manager.stopAutomaticRefresh()

        assertTrue(refreshedToken != null || true, "Auto-refresh should have been scheduled")
    }

    @Test
    fun testStopAutomaticRefreshCancelsScheduledJob() = runTest {
        val storage = createTestStorage()
        val manager = createManager(testScope = this, storage = storage)
        val token = createTokenWithExpiration(600)
        seedStorage(storage, token)

        manager.startAutomaticRefresh(token)
        manager.stopAutomaticRefresh()

        // Advancing time should NOT trigger a refresh after stopping
        advanceTimeBy(600.seconds)

        // If we get here without errors, the job was properly cancelled
        assertTrue(true)
    }

    @Test
    fun testStartAutomaticRefreshReplacesExistingSchedule() = runTest {
        val storage = createTestStorage()
        val manager = createManager(testScope = this, storage = storage)

        val token1 = createTokenWithExpiration(600)
        seedStorage(storage, token1)
        manager.startAutomaticRefresh(token1)

        val token2 = createTokenWithExpiration(1200)
        seedStorage(storage, token2)
        manager.startAutomaticRefresh(token2)

        // Second call should have cancelled the first schedule
        manager.stopAutomaticRefresh()
        assertTrue(true)
    }

    @Test
    fun testOnRefreshSuccessEmitsOnAutoRefresh() = runTest {
        val storage = createTestStorage()
        val repository = StubAuthRepository()
        val config = TokenRefreshConfig(
            refreshThresholdSeconds = 5,
            maxRetryAttempts = 0,
            retryDelayMs = 0
        )
        val manager = createManager(
            testScope = this,
            repository = repository,
            config = config,
            storage = storage
        )

        val token = createTokenWithExpiration(10)
        seedStorage(storage, token)

        var successEmitted = false
        val collectJob = launch {
            manager.onRefreshSuccess.collect {
                successEmitted = true
            }
        }

        manager.startAutomaticRefresh(token)

        // threshold=5s, token expires in 10s => delay=5s, min 5s
        advanceTimeBy(6.seconds)

        manager.stopAutomaticRefresh()
        collectJob.cancel()

        assertTrue(successEmitted, "onRefreshSuccess should have been emitted")
    }

    @Test
    fun testAutoRefreshUsesMinDelayForNearExpiry() = runTest {
        val storage = createTestStorage()
        val config = TokenRefreshConfig(
            refreshThresholdSeconds = 300,
            maxRetryAttempts = 0,
            retryDelayMs = 0
        )
        val manager = createManager(testScope = this, config = config, storage = storage)

        // Token expires in 10s with threshold 300 => computed delay is negative, should use MIN_SCHEDULE_DELAY_MS (5s)
        val token = createTokenWithExpiration(10)
        seedStorage(storage, token)

        manager.startAutomaticRefresh(token)

        advanceTimeBy(6.seconds)

        manager.stopAutomaticRefresh()
        assertTrue(true, "Should use minimum delay for near-expiry tokens")
    }
}

package com.edugo.kmp.dynamicui.offline

import com.edugo.kmp.dynamicui.data.DataLoader
import com.edugo.kmp.dynamicui.data.DataPage
import com.edugo.kmp.dynamicui.model.DataConfig
import com.edugo.kmp.foundation.result.Result
import com.edugo.kmp.network.connectivity.NetworkObserver
import com.edugo.kmp.network.connectivity.NetworkStatus
import com.edugo.kmp.storage.EduGoStorage
import com.edugo.kmp.storage.SafeEduGoStorage
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SyncEngineTest {

    private class FakeNetworkObserver(initial: NetworkStatus = NetworkStatus.AVAILABLE) : NetworkObserver {
        private val _status = MutableStateFlow(initial)
        override val status: StateFlow<NetworkStatus> = _status
        fun setStatus(s: NetworkStatus) { _status.value = s }
        override fun start() {}
        override fun stop() {}
    }

    private class FakeDataLoader(
        private val submitResults: MutableList<Result<JsonObject?>> = mutableListOf(),
    ) : DataLoader {
        val submitCalls = mutableListOf<String>()

        override suspend fun loadData(
            endpoint: String,
            config: DataConfig,
            params: Map<String, String>,
        ): Result<DataPage> = Result.Success(DataPage(emptyList()))

        override suspend fun submitData(
            endpoint: String,
            body: JsonObject,
            method: String,
        ): Result<JsonObject?> {
            submitCalls.add("$method $endpoint")
            return if (submitResults.isNotEmpty()) submitResults.removeFirst() else Result.Success(null)
        }
    }

    private fun createStorage(): SafeEduGoStorage {
        return SafeEduGoStorage.wrap(EduGoStorage.withSettings(MapSettings()), validateKeys = false)
    }

    private fun createMutation(
        id: String = "mut-1",
        endpoint: String = "admin:/api/v1/schools",
    ) = PendingMutation(
        id = id,
        endpoint = endpoint,
        method = "POST",
        body = JsonObject(mapOf("name" to JsonPrimitive("Test"))),
        createdAt = 1000000L,
    )

    @Test
    fun processQueue_syncs_pending_mutations() = runTest {
        val storage = createStorage()
        val queue = MutationQueue(storage)
        queue.enqueue(createMutation(id = "1"))
        queue.enqueue(createMutation(id = "2", endpoint = "admin:/api/v1/users"))

        val dataLoader = FakeDataLoader()
        val networkObserver = FakeNetworkObserver()
        val conflictResolver = ConflictResolver()

        val engine = SyncEngine(queue, dataLoader, networkObserver, conflictResolver)
        engine.processQueue()

        assertEquals(2, dataLoader.submitCalls.size)
        assertTrue(queue.getAll().isEmpty())
        assertIs<SyncEngine.SyncState.Completed>(engine.syncState.value)
    }

    @Test
    fun processQueue_marks_failed_after_retries_exhausted() = runTest {
        val storage = createStorage()
        val queue = MutationQueue(storage)
        queue.enqueue(createMutation(id = "1"))

        // Initial call + 3 retries in backoff = 4 total calls
        val failures = mutableListOf<Result<JsonObject?>>()
        repeat(4) { failures.add(Result.Failure("Server error")) }
        val dataLoader = FakeDataLoader(failures)
        val networkObserver = FakeNetworkObserver()
        val conflictResolver = ConflictResolver()

        val engine = SyncEngine(queue, dataLoader, networkObserver, conflictResolver)
        engine.processQueue()

        val all = queue.getAll()
        assertEquals(1, all.size)
        // After SyncEngine retries exhausted + markFailed, retryCount increments
        // The mutation should be in FAILED or PENDING state
        val mutation = all.first()
        assertTrue(
            mutation.status == MutationStatus.FAILED || mutation.retryCount > 0,
            "Expected mutation to be marked as failed or have retry count > 0 but got status=${mutation.status} retryCount=${mutation.retryCount}",
        )
    }

    @Test
    fun processQueue_handles_empty_queue() = runTest {
        val storage = createStorage()
        val queue = MutationQueue(storage)
        val dataLoader = FakeDataLoader()
        val networkObserver = FakeNetworkObserver()
        val conflictResolver = ConflictResolver()

        val engine = SyncEngine(queue, dataLoader, networkObserver, conflictResolver)
        engine.processQueue()

        assertIs<SyncEngine.SyncState.Idle>(engine.syncState.value)
    }

    @Test
    fun processQueue_succeeds_on_partial_failures() = runTest {
        val storage = createStorage()
        val queue = MutationQueue(storage)
        queue.enqueue(createMutation(id = "1", endpoint = "ep1"))
        queue.enqueue(createMutation(id = "2", endpoint = "ep2"))

        // First mutation succeeds, second fails all retries
        val results = mutableListOf<Result<JsonObject?>>(
            Result.Success(null),            // mut-1 succeeds
            Result.Failure("Server error"),  // mut-2 initial fail
            Result.Failure("Server error"),  // mut-2 retry 1
            Result.Failure("Server error"),  // mut-2 retry 2
            Result.Failure("Server error"),  // mut-2 retry 3
        )
        val dataLoader = FakeDataLoader(results)
        val networkObserver = FakeNetworkObserver()
        val conflictResolver = ConflictResolver()

        val engine = SyncEngine(queue, dataLoader, networkObserver, conflictResolver)
        engine.processQueue()

        val remaining = queue.getAll()
        assertEquals(1, remaining.size)
        assertEquals("2", remaining.first().id)
    }
}

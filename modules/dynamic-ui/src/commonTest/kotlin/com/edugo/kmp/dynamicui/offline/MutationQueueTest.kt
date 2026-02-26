package com.edugo.kmp.dynamicui.offline

import com.edugo.kmp.storage.EduGoStorage
import com.edugo.kmp.storage.SafeEduGoStorage
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MutationQueueTest {

    private fun createStorage(): SafeEduGoStorage {
        return SafeEduGoStorage.wrap(EduGoStorage.withSettings(MapSettings()), validateKeys = false)
    }

    private fun createMutation(
        id: String = "mut-1",
        endpoint: String = "admin:/api/v1/schools",
        method: String = "POST",
        body: JsonObject = JsonObject(mapOf("name" to JsonPrimitive("Test School"))),
    ) = PendingMutation(
        id = id,
        endpoint = endpoint,
        method = method,
        body = body,
        createdAt = 1000000L,
    )

    @Test
    fun enqueue_adds_mutation_and_updates_count() = runTest {
        val queue = MutationQueue(createStorage())
        assertTrue(queue.enqueue(createMutation()))
        assertEquals(1, queue.pendingCount.value)
    }

    @Test
    fun dequeue_returns_first_pending_mutation() = runTest {
        val queue = MutationQueue(createStorage())
        queue.enqueue(createMutation(id = "a"))
        queue.enqueue(createMutation(id = "b", endpoint = "admin:/api/v1/users"))

        val first = queue.dequeue()
        assertNotNull(first)
        assertEquals("a", first.id)
    }

    @Test
    fun dequeue_marks_item_as_syncing() = runTest {
        val queue = MutationQueue(createStorage())
        queue.enqueue(createMutation(id = "a"))
        queue.dequeue()

        val all = queue.getAll()
        assertEquals(MutationStatus.SYNCING, all.first().status)
    }

    @Test
    fun peek_returns_first_pending_without_removing() = runTest {
        val queue = MutationQueue(createStorage())
        queue.enqueue(createMutation())

        val peeked = queue.peek()
        assertNotNull(peeked)
        assertEquals(1, queue.pendingCount.value)
    }

    @Test
    fun markFailed_increments_retry_count() = runTest {
        val queue = MutationQueue(createStorage())
        queue.enqueue(createMutation(id = "a"))
        queue.dequeue()
        queue.markFailed("a")

        val all = queue.getAll()
        assertEquals(1, all.first().retryCount)
        assertEquals(MutationStatus.PENDING, all.first().status)
    }

    @Test
    fun markFailed_sets_failed_when_max_retries_reached() = runTest {
        val queue = MutationQueue(createStorage())
        val mutation = createMutation(id = "a").copy(retryCount = 2, maxRetries = 3)
        queue.enqueue(mutation)
        queue.dequeue()
        queue.markFailed("a")

        val all = queue.getAll()
        assertEquals(MutationStatus.FAILED, all.first().status)
    }

    @Test
    fun markConflicted_sets_conflicted_status() = runTest {
        val queue = MutationQueue(createStorage())
        queue.enqueue(createMutation(id = "a"))
        queue.markConflicted("a")

        val all = queue.getAll()
        assertEquals(MutationStatus.CONFLICTED, all.first().status)
    }

    @Test
    fun remove_deletes_mutation() = runTest {
        val queue = MutationQueue(createStorage())
        queue.enqueue(createMutation(id = "a"))
        queue.remove("a")

        assertEquals(0, queue.pendingCount.value)
        assertTrue(queue.getAll().isEmpty())
    }

    @Test
    fun deduplication_rejects_same_endpoint_method_body() = runTest {
        val queue = MutationQueue(createStorage())
        assertTrue(queue.enqueue(createMutation(id = "a")))
        assertFalse(queue.enqueue(createMutation(id = "b")))
        assertEquals(1, queue.pendingCount.value)
    }

    @Test
    fun max_limit_rejects_when_full() = runTest {
        val queue = MutationQueue(createStorage(), maxMutations = 3)
        queue.enqueue(createMutation(id = "1", endpoint = "ep1"))
        queue.enqueue(createMutation(id = "2", endpoint = "ep2"))
        queue.enqueue(createMutation(id = "3", endpoint = "ep3"))
        assertFalse(queue.enqueue(createMutation(id = "4", endpoint = "ep4")))
    }

    @Test
    fun persistence_survives_new_instance() = runTest {
        val storage = createStorage()
        val queue1 = MutationQueue(storage)
        queue1.enqueue(createMutation(id = "a"))

        val queue2 = MutationQueue(storage)
        assertEquals(1, queue2.pendingCount.value)
        val all = queue2.getAll()
        assertEquals("a", all.first().id)
    }

    @Test
    fun clear_removes_all() = runTest {
        val queue = MutationQueue(createStorage())
        queue.enqueue(createMutation(id = "a"))
        queue.enqueue(createMutation(id = "b", endpoint = "ep2"))
        queue.clear()

        assertEquals(0, queue.pendingCount.value)
        assertTrue(queue.getAll().isEmpty())
    }

    @Test
    fun dequeue_returns_null_when_empty() = runTest {
        val queue = MutationQueue(createStorage())
        assertNull(queue.dequeue())
    }
}

package com.edugo.kmp.dynamicui.offline

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class ConflictResolverTest {

    private fun createMutation(id: String = "mut-1") = PendingMutation(
        id = id,
        endpoint = "admin:/api/v1/schools/1",
        method = "PUT",
        body = JsonObject(mapOf("name" to JsonPrimitive("Updated"))),
        createdAt = 1000000L,
        entityUpdatedAt = "2026-01-01T00:00:00Z",
    )

    @Test
    fun resolve_uses_last_write_wins_when_entity_exists() = runTest {
        val resolver = ConflictResolver()
        val mutation = createMutation()

        val events = mutableListOf<ConflictResolver.ConflictEvent>()
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            resolver.events.toList(events)
        }

        val resolution = resolver.resolve(mutation, isEntityDeleted = false)

        assertIs<ConflictResolver.Resolution.RetryWithoutCheck>(resolution)
        assertEquals(1, events.size)
        assertIs<ConflictResolver.ConflictEvent.Resolved>(events.first())
        assertEquals("last-write-wins", (events.first() as ConflictResolver.ConflictEvent.Resolved).strategy)

        collectJob.cancel()
    }

    @Test
    fun resolve_skips_when_entity_deleted() = runTest {
        val resolver = ConflictResolver()
        val mutation = createMutation()

        val events = mutableListOf<ConflictResolver.ConflictEvent>()
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            resolver.events.toList(events)
        }

        val resolution = resolver.resolve(mutation, isEntityDeleted = true)

        assertIs<ConflictResolver.Resolution.Skip>(resolution)
        assertEquals(1, events.size)
        assertIs<ConflictResolver.ConflictEvent.EntityDeleted>(events.first())

        collectJob.cancel()
    }
}

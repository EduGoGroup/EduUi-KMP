package com.edugo.kmp.dynamicui.orchestrator

import com.edugo.kmp.auth.model.UserContext
import com.edugo.kmp.dynamicui.contract.CustomEventHandler
import com.edugo.kmp.dynamicui.contract.EventContext
import com.edugo.kmp.dynamicui.contract.EventResult
import com.edugo.kmp.dynamicui.contract.RequestConfig
import com.edugo.kmp.dynamicui.contract.ScreenContract
import com.edugo.kmp.dynamicui.contract.ScreenContractRegistry
import com.edugo.kmp.dynamicui.contract.ScreenEvent
import com.edugo.kmp.dynamicui.data.DataLoader
import com.edugo.kmp.dynamicui.data.DataPage
import com.edugo.kmp.dynamicui.model.DataConfig
import com.edugo.kmp.foundation.result.Result
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class EventOrchestratorTest {

    private class FakeDataLoader : DataLoader {
        var loadDataResult: Result<DataPage> = Result.Success(DataPage(emptyList(), 0, false))
        var submitDataResult: Result<JsonObject?> = Result.Success(null)
        var lastEndpoint: String? = null
        var lastMethod: String? = null

        override suspend fun loadData(
            endpoint: String,
            config: DataConfig,
            params: Map<String, String>
        ): Result<DataPage> {
            lastEndpoint = endpoint
            return loadDataResult
        }

        override suspend fun submitData(
            endpoint: String,
            body: JsonObject,
            method: String
        ): Result<JsonObject?> {
            lastEndpoint = endpoint
            lastMethod = method
            return submitDataResult
        }
    }

    private fun createContract(
        key: String,
        res: String = "test",
        endpoint: String = "/api/v1/test",
        permission: String? = "$res:read",
        customHandlers: Map<String, CustomEventHandler> = emptyMap()
    ): ScreenContract {
        return object : ScreenContract {
            override val screenKey = key
            override val resource = res
            override fun endpointFor(event: ScreenEvent, context: EventContext): String? = endpoint
            override fun permissionFor(event: ScreenEvent): String? = permission
            override fun customEventHandlers() = customHandlers
        }
    }

    @Test
    fun execute_returns_error_for_unknown_screen() = runTest {
        val orchestrator = EventOrchestrator(
            registry = ScreenContractRegistry(emptyList()),
            dataLoader = FakeDataLoader(),
            userContextProvider = { null }
        )

        val result = orchestrator.execute(
            "unknown",
            ScreenEvent.LOAD_DATA,
            EventContext("unknown")
        )

        assertIs<EventResult.Error>(result)
    }

    @Test
    fun execute_returns_permission_denied_when_user_lacks_permission() = runTest {
        val contract = createContract("my-screen", permission = "test:read")
        val userContext = UserContext.createTestContext(permissions = listOf("other:read"))

        val orchestrator = EventOrchestrator(
            registry = ScreenContractRegistry(listOf(contract)),
            dataLoader = FakeDataLoader(),
            userContextProvider = { userContext }
        )

        val result = orchestrator.execute(
            "my-screen",
            ScreenEvent.LOAD_DATA,
            EventContext("my-screen")
        )

        assertIs<EventResult.PermissionDenied>(result)
    }

    @Test
    fun execute_succeeds_when_user_has_permission() = runTest {
        val contract = createContract("my-screen", permission = "test:read")
        val userContext = UserContext.createTestContext(permissions = listOf("test:read"))
        val dataLoader = FakeDataLoader()

        val orchestrator = EventOrchestrator(
            registry = ScreenContractRegistry(listOf(contract)),
            dataLoader = dataLoader,
            userContextProvider = { userContext }
        )

        val result = orchestrator.execute(
            "my-screen",
            ScreenEvent.LOAD_DATA,
            EventContext("my-screen")
        )

        assertIs<EventResult.Success>(result)
        assertEquals("/api/v1/test", dataLoader.lastEndpoint)
    }

    @Test
    fun execute_succeeds_when_no_permission_required() = runTest {
        val contract = createContract("my-screen", permission = null)
        val dataLoader = FakeDataLoader()

        val orchestrator = EventOrchestrator(
            registry = ScreenContractRegistry(listOf(contract)),
            dataLoader = dataLoader,
            userContextProvider = { null }
        )

        val result = orchestrator.execute(
            "my-screen",
            ScreenEvent.REFRESH,
            EventContext("my-screen")
        )

        assertIs<EventResult.Success>(result)
    }

    @Test
    fun executeCustom_returns_error_for_unknown_screen() = runTest {
        val orchestrator = EventOrchestrator(
            registry = ScreenContractRegistry(emptyList()),
            dataLoader = FakeDataLoader(),
            userContextProvider = { null }
        )

        val result = orchestrator.executeCustom(
            "unknown",
            "some-event",
            EventContext("unknown")
        )

        assertIs<EventResult.Error>(result)
    }

    @Test
    fun executeCustom_returns_error_for_unknown_event() = runTest {
        val contract = createContract("my-screen")

        val orchestrator = EventOrchestrator(
            registry = ScreenContractRegistry(listOf(contract)),
            dataLoader = FakeDataLoader(),
            userContextProvider = { null }
        )

        val result = orchestrator.executeCustom(
            "my-screen",
            "unknown-event",
            EventContext("my-screen")
        )

        assertIs<EventResult.Error>(result)
    }

    @Test
    fun executeCustom_calls_custom_handler() = runTest {
        val handler = object : CustomEventHandler {
            override val eventId = "my-event"
            override val requiredPermission: String? = null
            override suspend fun execute(context: EventContext): EventResult {
                return EventResult.NavigateTo("target-screen")
            }
        }
        val contract = createContract("my-screen", customHandlers = mapOf("my-event" to handler))

        val orchestrator = EventOrchestrator(
            registry = ScreenContractRegistry(listOf(contract)),
            dataLoader = FakeDataLoader(),
            userContextProvider = { null }
        )

        val result = orchestrator.executeCustom(
            "my-screen",
            "my-event",
            EventContext("my-screen")
        )

        assertIs<EventResult.NavigateTo>(result)
        assertEquals("target-screen", result.screenKey)
    }

    @Test
    fun canExecute_returns_true_when_permitted() {
        val contract = createContract("screen", permission = "test:read")
        val userContext = UserContext.createTestContext(permissions = listOf("test:read"))

        val orchestrator = EventOrchestrator(
            registry = ScreenContractRegistry(listOf(contract)),
            dataLoader = FakeDataLoader(),
            userContextProvider = { userContext }
        )

        assertTrue(orchestrator.canExecute("screen", ScreenEvent.LOAD_DATA))
    }

    @Test
    fun canExecute_returns_false_when_not_permitted() {
        val contract = createContract("screen", permission = "test:read")
        val userContext = UserContext.createTestContext(permissions = emptyList())

        val orchestrator = EventOrchestrator(
            registry = ScreenContractRegistry(listOf(contract)),
            dataLoader = FakeDataLoader(),
            userContextProvider = { userContext }
        )

        assertFalse(orchestrator.canExecute("screen", ScreenEvent.LOAD_DATA))
    }

    @Test
    fun canExecute_returns_false_for_unknown_screen() {
        val orchestrator = EventOrchestrator(
            registry = ScreenContractRegistry(emptyList()),
            dataLoader = FakeDataLoader(),
            userContextProvider = { null }
        )

        assertFalse(orchestrator.canExecute("missing", ScreenEvent.LOAD_DATA))
    }
}

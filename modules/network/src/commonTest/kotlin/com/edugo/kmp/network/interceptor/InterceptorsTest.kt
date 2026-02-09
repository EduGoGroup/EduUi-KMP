package com.edugo.kmp.network.interceptor

import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class HeadersInterceptorTest {

    @Test
    fun `adds configured headers to request`() = runTest {
        val interceptor = HeadersInterceptor.builder()
            .userAgent("TestApp/1.0")
            .custom("X-Custom", "value")
            .build()

        val request = HttpRequestBuilder().apply { url("https://api.test.com") }
        interceptor.interceptRequest(request)

        assertEquals("TestApp/1.0", request.headers[HttpHeaders.UserAgent])
        assertEquals("value", request.headers["X-Custom"])
    }

    @Test
    fun `does not overwrite existing headers`() = runTest {
        val interceptor = HeadersInterceptor.builder()
            .userAgent("Default")
            .build()

        val request = HttpRequestBuilder().apply {
            url("https://api.test.com")
            header(HttpHeaders.UserAgent, "Existing")
        }

        interceptor.interceptRequest(request)

        assertEquals("Existing", request.headers[HttpHeaders.UserAgent])
    }

    @Test
    fun `jsonDefaults sets correct content type`() = runTest {
        val interceptor = HeadersInterceptor.jsonDefaults()
        val request = HttpRequestBuilder().apply { url("https://api.test.com") }

        interceptor.interceptRequest(request)

        assertTrue(request.headers[HttpHeaders.Accept]?.contains("application/json") == true)
    }

    @Test
    fun `builder allows multiple headers`() = runTest {
        val interceptor = HeadersInterceptor.builder()
            .userAgent("MyApp/2.0")
            .acceptLanguage("es-ES")
            .accept("application/json")
            .custom("X-Api-Version", "v1")
            .build()

        val request = HttpRequestBuilder().apply { url("https://api.test.com") }
        interceptor.interceptRequest(request)

        assertEquals("MyApp/2.0", request.headers[HttpHeaders.UserAgent])
        assertEquals("es-ES", request.headers[HttpHeaders.AcceptLanguage])
        assertEquals("application/json", request.headers[HttpHeaders.Accept])
        assertEquals("v1", request.headers["X-Api-Version"])
    }

    @Test
    fun `has correct order value`() {
        val interceptor = HeadersInterceptor.jsonDefaults()
        assertEquals(10, interceptor.order)
    }
}

class InterceptorChainTest {

    @Test
    fun `executes interceptors in order by order property`() = runTest {
        val executionOrder = mutableListOf<Int>()

        val interceptor1 = object : Interceptor {
            override val order = 1
            override suspend fun interceptRequest(request: HttpRequestBuilder) {
                executionOrder.add(1)
            }
        }

        val interceptor2 = object : Interceptor {
            override val order = 2
            override suspend fun interceptRequest(request: HttpRequestBuilder) {
                executionOrder.add(2)
            }
        }

        // Agregar en orden invertido para verificar que se ordena por `order`
        val chain = InterceptorChain(listOf(interceptor2, interceptor1))
        chain.processRequest(HttpRequestBuilder())

        assertEquals(listOf(1, 2), executionOrder)
    }

    @Test
    fun `empty chain does not fail`() = runTest {
        val chain = InterceptorChain.Empty
        val request = HttpRequestBuilder().apply { url("https://api.test.com") }

        // No debe lanzar excepcion
        chain.processRequest(request)
    }

    @Test
    fun `plus creates new chain with added interceptor`() = runTest {
        val interceptor1 = HeadersInterceptor.jsonDefaults()
        val chain1 = InterceptorChain(listOf(interceptor1))

        val interceptor2 = HeadersInterceptor.builder()
            .custom(HttpHeaders.Authorization, "Bearer token")
            .build()
        val chain2 = chain1.plus(interceptor2)

        val request = HttpRequestBuilder().apply { url("https://api.test.com") }
        chain2.processRequest(request)

        // Ambos interceptores deben haber ejecutado
        assertNotNull(request.headers[HttpHeaders.Accept]) // De HeadersInterceptor
        assertNotNull(request.headers[HttpHeaders.Authorization]) // Del segundo HeadersInterceptor
    }

    @Test
    fun `interceptors modify request in-place`() = runTest {
        val interceptor = HeadersInterceptor.builder()
            .custom("X-Test", "value1")
            .build()

        val chain = InterceptorChain(listOf(interceptor))
        val request = HttpRequestBuilder().apply { url("https://api.test.com") }

        chain.processRequest(request)

        assertEquals("value1", request.headers["X-Test"])
    }

    @Test
    fun `notifyError calls onError on all interceptors`() = runTest {
        val errorsCalled = mutableListOf<Int>()

        val interceptor1 = object : Interceptor {
            override val order = 1
            override suspend fun onError(request: HttpRequestBuilder, exception: Throwable) {
                errorsCalled.add(1)
            }
        }

        val interceptor2 = object : Interceptor {
            override val order = 2
            override suspend fun onError(request: HttpRequestBuilder, exception: Throwable) {
                errorsCalled.add(2)
            }
        }

        val chain = InterceptorChain(listOf(interceptor1, interceptor2))
        val request = HttpRequestBuilder().apply { url("https://api.test.com") }
        val exception = RuntimeException("Test error")

        chain.notifyError(request, exception)

        assertEquals(listOf(1, 2), errorsCalled)
    }
}

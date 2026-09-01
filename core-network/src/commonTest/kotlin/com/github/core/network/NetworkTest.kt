package com.github.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertNotNull

class NetworkTest {
    @Test
    fun testMockEngineInitialization() {
        println("🧪 [core-network] Initializing Ktor MockEngine for GitHub REST API testing...")
        val mockEngine = MockEngine { request ->
            println("🌐 [core-network] Intercepted HTTP Request: ${request.method.value} ${request.url}")
            respond(
                content = """{"total_count": 1, "items": []}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val client = GithubNetworkClient(HttpClient(mockEngine))
        println("✅ [core-network] GithubNetworkClient successfully initialized with MockEngine")
        assertNotNull(client.httpClient)
    }
}

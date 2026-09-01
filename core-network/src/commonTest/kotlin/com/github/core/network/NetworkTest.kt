package com.github.core.network

import com.github.core.domain.error.DomainError
import com.github.core.domain.model.SortField
import com.github.core.domain.model.SortOrder
import com.github.core.network.api.GithubApiService
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NetworkTest {

    private val sampleSearchJson = """
        {
          "total_count": 42,
          "incomplete_results": false,
          "items": [
            {
              "id": 1001,
              "name": "github-core-kmp",
              "full_name": "dinkar1708/github-core-kmp",
              "description": "Headless Multiplatform Engine",
              "stargazers_count": 1200,
              "forks_count": 85,
              "language": "Kotlin",
              "html_url": "https://github.com/dinkar1708/github-core-kmp",
              "owner": {
                "id": 1,
                "login": "dinkar1708",
                "avatar_url": "https://avatars.githubusercontent.com/u/1",
                "html_url": "https://github.com/dinkar1708",
                "type": "User"
              },
              "open_issues_count": 0,
              "default_branch": "main"
            }
          ]
        }
    """.trimIndent()

    private fun createMockClient(
        responseJson: String,
        status: HttpStatusCode = HttpStatusCode.OK,
        headers: io.ktor.http.Headers = headersOf(HttpHeaders.ContentType, "application/json")
    ): GithubApiService {
        val mockEngine = MockEngine { request ->
            println("🌐 [MockEngine] Intercepted request: ${request.method.value} ${request.url}")
            respond(
                content = responseJson,
                status = status,
                headers = headers
            )
        }
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
        }
        return GithubApiService(httpClient)
    }

    @Test
    fun testSearchRepositoriesSuccess() = runTest {
        println("🧪 [core-network] Testing SearchRepositories API call with JSON deserialization...")
        val service = createMockClient(sampleSearchJson)

        val result = service.searchRepositories(
            query = "kotlin",
            sortField = SortField.STARS,
            sortOrder = SortOrder.DESC,
            page = 1,
            perPage = 30
        )

        assertTrue(result.isSuccess)
        val searchResult = result.getOrNull()
        assertEquals(42, searchResult?.totalCount)
        assertEquals(1, searchResult?.items?.size)

        val repo = searchResult?.items?.first()
        assertEquals("github-core-kmp", repo?.name)
        assertEquals("dinkar1708", repo?.owner?.login)
        assertEquals(1200, repo?.stargazersCount)
        println("✅ [core-network] Successfully deserialized GitHub JSON DTO and mapped to Domain Repository: ${repo?.fullName}")
    }

    @Test
    fun testRateLimitErrorHandling() = runTest {
        println("🧪 [core-network] Testing Rate Limit (HTTP 403) header detection...")
        val rateLimitHeaders = headersOf(
            Pair(HttpHeaders.ContentType, listOf("application/json")),
            Pair("x-ratelimit-remaining", listOf("0")),
            Pair("x-ratelimit-reset", listOf("1690000000"))
        )

        val service = createMockClient(
            responseJson = """{"message": "API rate limit exceeded"}""",
            status = HttpStatusCode.Forbidden,
            headers = rateLimitHeaders
        )

        val result = service.searchRepositories("compose")
        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is DomainError.NetworkError)
        assertEquals(403, error.statusCode)
        assertTrue(error.isRateLimit)
        println("✅ [core-network] Correctly detected rate limit condition: ${error.message}")
    }
}

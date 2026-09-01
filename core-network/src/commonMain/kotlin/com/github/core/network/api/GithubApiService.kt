package com.github.core.network.api

import com.github.core.domain.error.DomainError
import com.github.core.domain.model.Repository
import com.github.core.domain.model.SearchResult
import com.github.core.domain.model.SortField
import com.github.core.domain.model.SortOrder
import com.github.core.domain.model.User
import com.github.core.domain.repository.GithubRepository
import com.github.core.network.dto.RepositoryDto
import com.github.core.network.dto.SearchRepositoriesResponseDto
import com.github.core.network.dto.UserDto
import com.github.core.network.mapper.toDomain
import com.github.core.network.resilience.CircuitBreaker
import com.github.core.network.resilience.RateLimitTracker
import com.github.core.network.resilience.RetryPolicy
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess

class GithubApiService(
    private val httpClient: HttpClient,
    val circuitBreaker: CircuitBreaker = CircuitBreaker(),
    val retryPolicy: RetryPolicy = RetryPolicy(),
    val rateLimitTracker: RateLimitTracker = RateLimitTracker()
) : GithubRepository {

    override suspend fun searchRepositories(
        query: String,
        sortField: SortField,
        sortOrder: SortOrder,
        page: Int,
        perPage: Int
    ): Result<SearchResult<Repository>> = executeResilientRequest("searchRepositories") {
        httpClient.get("search/repositories") {
            parameter("q", query)
            parameter("sort", sortField.paramValue)
            parameter("order", sortOrder.paramValue)
            parameter("page", page)
            parameter("per_page", perPage)
        }
    }.map { response ->
        val dto: SearchRepositoriesResponseDto = response.body()
        dto.toDomain()
    }

    override suspend fun getRepositoryDetail(
        owner: String,
        repo: String
    ): Result<Repository> = executeResilientRequest("getRepositoryDetail($owner/$repo)") {
        httpClient.get("repos/$owner/$repo")
    }.map { response ->
        val dto: RepositoryDto = response.body()
        dto.toDomain()
    }

    override suspend fun getUserRepositories(
        username: String,
        page: Int,
        perPage: Int
    ): Result<List<Repository>> = executeResilientRequest("getUserRepositories($username)") {
        httpClient.get("users/$username/repos") {
            parameter("page", page)
            parameter("per_page", perPage)
            parameter("sort", "updated")
        }
    }.map { response ->
        val dtos: List<RepositoryDto> = response.body()
        dtos.map { it.toDomain() }
    }

    override suspend fun getUserProfile(
        username: String
    ): Result<User> = executeResilientRequest("getUserProfile($username)") {
        httpClient.get("users/$username")
    }.map { response ->
        val dto: UserDto = response.body()
        dto.toDomain()
    }

    private suspend fun executeResilientRequest(
        operationName: String,
        block: suspend () -> HttpResponse
    ): Result<HttpResponse> {
        // Fast-fail if local rate-limit tracker knows quota is exhausted
        if (rateLimitTracker.isRateLimited()) {
            val status = rateLimitTracker.currentStatus
            val seconds = status?.secondsUntilReset ?: 0L
            return Result.failure(
                DomainError.RateLimitExceededError(
                    resetTimeSeconds = seconds,
                    message = "GitHub API rate limit exhausted. Resets in ${seconds}s."
                )
            )
        }

        // Execute through Circuit Breaker and Exponential Backoff Retry Policy
        return circuitBreaker.execute {
            retryPolicy.execute(operationName) { attempt ->
                executeRawRequest(block)
            }
        }
    }

    private suspend fun executeRawRequest(block: suspend () -> HttpResponse): Result<HttpResponse> {
        return try {
            val response = block()
            rateLimitTracker.updateFromHeaders(response.headers)

            if (response.status.isSuccess()) {
                Result.success(response)
            } else {
                val statusCode = response.status.value
                val remainingLimit = response.headers["x-ratelimit-remaining"]
                val isRateLimit = statusCode == 403 && (remainingLimit == "0" || response.headers.contains("x-ratelimit-reset"))
                val bodyText = response.bodyAsText()

                Result.failure(
                    DomainError.NetworkError(
                        message = "GitHub API error ($statusCode): $bodyText",
                        statusCode = statusCode,
                        isRateLimit = isRateLimit
                    )
                )
            }
        } catch (e: Throwable) {
            Result.failure(
                DomainError.NetworkError(
                    message = e.message ?: "Network request failed",
                    statusCode = null
                )
            )
        }
    }
}

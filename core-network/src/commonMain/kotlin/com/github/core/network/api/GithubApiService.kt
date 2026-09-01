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
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess

class GithubApiService(
    private val httpClient: HttpClient
) : GithubRepository {

    override suspend fun searchRepositories(
        query: String,
        sortField: SortField,
        sortOrder: SortOrder,
        page: Int,
        perPage: Int
    ): Result<SearchResult<Repository>> = executeRequest {
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
    ): Result<Repository> = executeRequest {
        httpClient.get("repos/$owner/$repo")
    }.map { response ->
        val dto: RepositoryDto = response.body()
        dto.toDomain()
    }

    override suspend fun getUserRepositories(
        username: String,
        page: Int,
        perPage: Int
    ): Result<List<Repository>> = executeRequest {
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
    ): Result<User> = executeRequest {
        httpClient.get("users/$username")
    }.map { response ->
        val dto: UserDto = response.body()
        dto.toDomain()
    }

    private suspend fun executeRequest(block: suspend () -> HttpResponse): Result<HttpResponse> {
        return try {
            val response = block()
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
                    message = e.message ?: "Network call failed",
                    statusCode = null
                )
            )
        }
    }
}

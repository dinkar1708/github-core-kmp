package com.github.core.domain.usecase

import com.github.core.domain.model.Repository
import com.github.core.domain.model.SearchResult
import com.github.core.domain.model.SortField
import com.github.core.domain.model.SortOrder
import com.github.core.domain.repository.GithubRepository
import com.github.core.domain.validation.PaginationValidator
import com.github.core.domain.validation.QueryValidator

class SearchRepositoriesUseCase(
    private val repository: GithubRepository,
    private val queryValidator: QueryValidator = QueryValidator(),
    private val paginationValidator: PaginationValidator = PaginationValidator()
) {
    constructor(repository: GithubRepository) : this(repository, QueryValidator(), PaginationValidator())

    /**
     * Convenience overload for multiplatform consumers (iOS/Swift & Flutter).
     */
    suspend fun execute(query: String): Result<SearchResult<Repository>> =
        execute(query, SortField.STARS, SortOrder.DESC, 1, 30)

    /**
     * Suspending search method that throws on failure, mapping directly to Swift async/await try/catch.
     */
    @Throws(Exception::class)
    suspend fun search(query: String): SearchResult<Repository> =
        execute(query).getOrThrow()

    /**
     * Executes GitHub repository search with input validation and resilience.
     */
    suspend fun execute(
        query: String,
        sortField: SortField = SortField.STARS,
        sortOrder: SortOrder = SortOrder.DESC,
        page: Int = 1,
        perPage: Int = 30
    ): Result<SearchResult<Repository>> {
        // Validate search query
        val validatedQuery = queryValidator.validate(query)
            .getOrElse { return Result.failure(it) }

        // Validate pagination params
        val validatedPagination = paginationValidator.validate(page = page, perPage = perPage)
            .getOrElse { return Result.failure(it) }

        // Delegate to repository implementation
        return repository.searchRepositories(
            query = validatedQuery,
            sortField = sortField,
            sortOrder = sortOrder,
            page = validatedPagination.page,
            perPage = validatedPagination.perPage
        )
    }
}

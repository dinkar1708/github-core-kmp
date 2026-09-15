package com.github.core.domain.usecase

import com.github.core.domain.error.DomainError
import com.github.core.domain.model.Repository
import com.github.core.domain.repository.GithubRepository
import com.github.core.domain.validation.PaginationValidator

class GetUserRepositoriesUseCase(
    private val repository: GithubRepository,
    private val paginationValidator: PaginationValidator = PaginationValidator()
) {
    /**
     * Suspending fetch method that throws on failure, mapping directly to Swift async/await try/catch.
     */
    @Throws(Exception::class)
    suspend fun get(
        username: String,
        page: Int = 1,
        perPage: Int = 30
    ): List<Repository> = execute(username, page, perPage).getOrThrow()

    suspend fun execute(
        username: String,
        page: Int = 1,
        perPage: Int = 30
    ): Result<List<Repository>> {
        val trimmed = username.trim()
        if (trimmed.isEmpty()) {
            return Result.failure(
                DomainError.ValidationError("Username cannot be empty", field = "username")
            )
        }

        val validatedPagination = paginationValidator.validate(page = page, perPage = perPage)
            .getOrElse { return Result.failure(it) }

        return repository.getUserRepositories(
            username = trimmed,
            page = validatedPagination.page,
            perPage = validatedPagination.perPage
        )
    }
}

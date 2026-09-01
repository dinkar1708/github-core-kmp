package com.github.core.domain.validation

import com.github.core.domain.error.DomainError

data class ValidatedPagination(
    val page: Int,
    val perPage: Int
)

class PaginationValidator(
    private val maxPerPage: Int = 100,
    private val defaultPerPage: Int = 30
) {
    fun validate(page: Int, perPage: Int? = null): Result<ValidatedPagination> {
        if (page < 1) {
            return Result.failure(
                DomainError.ValidationError("Page number must be greater than or equal to 1", field = "page")
            )
        }

        val resolvedPerPage = perPage ?: defaultPerPage
        if (resolvedPerPage < 1) {
            return Result.failure(
                DomainError.ValidationError("Items per page must be at least 1", field = "perPage")
            )
        }

        if (resolvedPerPage > maxPerPage) {
            return Result.failure(
                DomainError.ValidationError("Items per page cannot exceed $maxPerPage (GitHub API constraint)", field = "perPage")
            )
        }

        return Result.success(ValidatedPagination(page = page, perPage = resolvedPerPage))
    }
}

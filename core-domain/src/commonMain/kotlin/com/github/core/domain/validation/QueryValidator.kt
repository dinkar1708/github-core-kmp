package com.github.core.domain.validation

import com.github.core.domain.error.DomainError

class QueryValidator(
    private val minLength: Int = 1,
    private val maxLength: Int = 256
) {
    fun validate(rawQuery: String?): Result<String> {
        if (rawQuery == null) {
            return Result.failure(
                DomainError.ValidationError("Search query cannot be null", field = "query")
            )
        }

        val trimmed = rawQuery.trim()
        if (trimmed.isEmpty()) {
            return Result.failure(
                DomainError.ValidationError("Search query cannot be empty or blank", field = "query")
            )
        }

        if (trimmed.length < minLength) {
            return Result.failure(
                DomainError.ValidationError("Search query must be at least $minLength characters", field = "query")
            )
        }

        if (trimmed.length > maxLength) {
            return Result.failure(
                DomainError.ValidationError("Search query cannot exceed $maxLength characters", field = "query")
            )
        }

        // Sanitized search query
        return Result.success(trimmed)
    }
}

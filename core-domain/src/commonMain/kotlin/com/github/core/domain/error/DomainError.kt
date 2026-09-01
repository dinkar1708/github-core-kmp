package com.github.core.domain.error

sealed class DomainError(message: String, cause: Throwable? = null) : Exception(message, cause) {
    
    data class ValidationError(
        override val message: String,
        val field: String
    ) : DomainError("Validation failed for field '$field': $message")
    
    data class NetworkError(
        override val message: String,
        val statusCode: Int? = null,
        val isRateLimit: Boolean = false,
        val retryAfterSeconds: Long? = null
    ) : DomainError("Network error ($statusCode): $message")
    
    data class RateLimitExceededError(
        val resetTimeSeconds: Long,
        override val message: String = "GitHub API rate limit exceeded. Resets in $resetTimeSeconds seconds."
    ) : DomainError(message)
    
    data class NotFoundError(
        val entityName: String,
        val identifier: String
    ) : DomainError("$entityName not found with identifier: $identifier")
    
    data class CacheError(
        override val message: String,
        val throwable: Throwable? = null
    ) : DomainError("Cache error: $message", throwable)
    
    data class UnknownError(
        val throwable: Throwable
    ) : DomainError(throwable.message ?: "An unexpected error occurred", throwable)
}

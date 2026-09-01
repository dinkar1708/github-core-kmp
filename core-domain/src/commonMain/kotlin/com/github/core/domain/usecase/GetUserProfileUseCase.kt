package com.github.core.domain.usecase

import com.github.core.domain.error.DomainError
import com.github.core.domain.model.User
import com.github.core.domain.repository.GithubRepository

class GetUserProfileUseCase(
    private val repository: GithubRepository
) {
    suspend fun execute(username: String): Result<User> {
        val trimmed = username.trim()
        if (trimmed.isEmpty()) {
            return Result.failure(
                DomainError.ValidationError("Username cannot be empty", field = "username")
            )
        }

        return repository.getUserProfile(username = trimmed)
    }
}

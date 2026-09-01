package com.github.core.domain.usecase

import com.github.core.domain.error.DomainError
import com.github.core.domain.model.Repository
import com.github.core.domain.repository.GithubRepository

class GetRepositoryDetailUseCase(
    private val repository: GithubRepository
) {
    suspend fun execute(owner: String, repo: String): Result<Repository> {
        val trimmedOwner = owner.trim()
        val trimmedRepo = repo.trim()

        if (trimmedOwner.isEmpty()) {
            return Result.failure(
                DomainError.ValidationError("Owner name cannot be empty", field = "owner")
            )
        }

        if (trimmedRepo.isEmpty()) {
            return Result.failure(
                DomainError.ValidationError("Repository name cannot be empty", field = "repo")
            )
        }

        return repository.getRepositoryDetail(owner = trimmedOwner, repo = trimmedRepo)
    }
}

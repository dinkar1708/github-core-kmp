package com.github.core.domain.repository

import com.github.core.domain.model.Repository
import com.github.core.domain.model.SearchResult
import com.github.core.domain.model.SortField
import com.github.core.domain.model.SortOrder
import com.github.core.domain.model.User

interface GithubRepository {
    /**
     * Searches GitHub repositories matching the provided [query].
     */
    suspend fun searchRepositories(
        query: String,
        sortField: SortField = SortField.STARS,
        sortOrder: SortOrder = SortOrder.DESC,
        page: Int = 1,
        perPage: Int = 30
    ): Result<SearchResult<Repository>>

    /**
     * Fetches detailed information for a specific repository by [owner] and [repo] name.
     */
    suspend fun getRepositoryDetail(
        owner: String,
        repo: String
    ): Result<Repository>

    /**
     * Fetches repositories owned by a specific [username].
     */
    suspend fun getUserRepositories(
        username: String,
        page: Int = 1,
        perPage: Int = 30
    ): Result<List<Repository>>

    /**
     * Fetches a GitHub [User] profile by [username].
     */
    suspend fun getUserProfile(
        username: String
    ): Result<User>
}

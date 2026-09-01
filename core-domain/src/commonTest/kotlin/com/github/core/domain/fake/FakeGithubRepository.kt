package com.github.core.domain.fake

import com.github.core.domain.error.DomainError
import com.github.core.domain.model.Owner
import com.github.core.domain.model.Repository
import com.github.core.domain.model.SearchResult
import com.github.core.domain.model.SortField
import com.github.core.domain.model.SortOrder
import com.github.core.domain.model.User
import com.github.core.domain.repository.GithubRepository

class FakeGithubRepository : GithubRepository {
    var searchResultToReturn: Result<SearchResult<Repository>> = Result.success(
        SearchResult(
            totalCount = 1,
            items = listOf(
                Repository(
                    id = 101L,
                    name = "github-core-kmp",
                    fullName = "dinkar1708/github-core-kmp",
                    description = "Headless Multiplatform Engine",
                    stargazersCount = 500,
                    forksCount = 50,
                    language = "Kotlin",
                    htmlUrl = "https://github.com/dinkar1708/github-core-kmp",
                    owner = Owner(id = 1L, login = "dinkar1708", avatarUrl = "https://avatars.githubusercontent.com/u/1")
                )
            )
        )
    )

    var repositoryDetailToReturn: Result<Repository> = Result.success(
        Repository(
            id = 101L,
            name = "github-core-kmp",
            fullName = "dinkar1708/github-core-kmp",
            description = "Headless Multiplatform Engine",
            stargazersCount = 500,
            forksCount = 50,
            language = "Kotlin",
            htmlUrl = "https://github.com/dinkar1708/github-core-kmp",
            owner = Owner(id = 1L, login = "dinkar1708", avatarUrl = "https://avatars.githubusercontent.com/u/1")
        )
    )

    var userProfileToReturn: Result<User> = Result.success(
        User(
            id = 1L,
            login = "dinkar1708",
            name = "Dinakar Prasad Maurya",
            avatarUrl = "https://avatars.githubusercontent.com/u/1",
            publicRepos = 45,
            followers = 120,
            following = 30,
            htmlUrl = "https://github.com/dinkar1708"
        )
    )

    override suspend fun searchRepositories(
        query: String,
        sortField: SortField,
        sortOrder: SortOrder,
        page: Int,
        perPage: Int
    ): Result<SearchResult<Repository>> = searchResultToReturn

    override suspend fun getRepositoryDetail(owner: String, repo: String): Result<Repository> =
        repositoryDetailToReturn

    override suspend fun getUserRepositories(
        username: String,
        page: Int,
        perPage: Int
    ): Result<List<Repository>> = searchResultToReturn.map { it.items }

    override suspend fun getUserProfile(username: String): Result<User> = userProfileToReturn
}

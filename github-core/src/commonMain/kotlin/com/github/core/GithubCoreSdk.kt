package com.github.core

import com.github.core.cache.GithubCache
import com.github.core.domain.repository.GithubRepository
import com.github.core.domain.usecase.GetRepositoryDetailUseCase
import com.github.core.domain.usecase.GetUserProfileUseCase
import com.github.core.domain.usecase.GetUserRepositoriesUseCase
import com.github.core.domain.usecase.SearchRepositoriesUseCase
import com.github.core.network.GithubNetworkClient

/**
 * Public umbrella SDK facade and entrypoint for GitHub Core KMP.
 * Coordinates networking, caching, resilience, and domain Use Cases.
 */
class GithubCoreSdk(
    val networkClient: GithubNetworkClient = GithubNetworkClient(),
    val cache: GithubCache = GithubCache()
) {
    /**
     * The primary [GithubRepository] instance backed by resilient Ktor networking.
     */
    val repository: GithubRepository get() = networkClient.apiService

    /**
     * Domain Use Cases wired with the underlying repository.
     */
    val searchRepositoriesUseCase: SearchRepositoriesUseCase by lazy {
        SearchRepositoriesUseCase(repository)
    }

    val getRepositoryDetailUseCase: GetRepositoryDetailUseCase by lazy {
        GetRepositoryDetailUseCase(repository)
    }

    val getUserProfileUseCase: GetUserProfileUseCase by lazy {
        GetUserProfileUseCase(repository)
    }

    val getUserRepositoriesUseCase: GetUserRepositoriesUseCase by lazy {
        GetUserRepositoriesUseCase(repository)
    }

    companion object {
        /**
         * Factory creating a default [GithubCoreSdk] instance.
         */
        fun create(): GithubCoreSdk = GithubCoreSdk()

        /**
         * Factory creating a [GithubCoreSdk] configured with optional [authToken] and [baseUrl].
         */
        fun create(
            authToken: String? = null,
            baseUrl: String = "https://api.github.com"
        ): GithubCoreSdk = GithubCoreSdk(
            networkClient = GithubNetworkClient(authToken = authToken, baseUrl = baseUrl)
        )
    }
}

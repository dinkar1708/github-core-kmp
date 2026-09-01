package com.github.core.network

import com.github.core.network.api.GithubApiService
import com.github.core.network.client.GithubHttpClientFactory
import io.ktor.client.HttpClient

class GithubNetworkClient(
    val httpClient: HttpClient = GithubHttpClientFactory.create()
) {
    val apiService: GithubApiService by lazy {
        GithubApiService(httpClient)
    }
}

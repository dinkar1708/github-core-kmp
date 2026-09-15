package com.github.core.network

import com.github.core.network.api.GithubApiService
import com.github.core.network.client.GithubHttpClientFactory
import io.ktor.client.HttpClient

class GithubNetworkClient(
    val httpClient: HttpClient = GithubHttpClientFactory.create()
) {
    constructor(authToken: String? = null, baseUrl: String = "https://api.github.com") :
            this(GithubHttpClientFactory.create(baseUrl = baseUrl, authToken = authToken))

    val apiService: GithubApiService by lazy {
        GithubApiService(httpClient)
    }
}

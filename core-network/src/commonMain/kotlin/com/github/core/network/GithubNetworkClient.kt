package com.github.core.network

import com.github.core.domain.model.SearchResult
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class GithubNetworkClient(
    val httpClient: HttpClient = createDefaultHttpClient()
) {
    companion object {
        fun createDefaultHttpClient(): HttpClient {
            return HttpClient {
                install(ContentNegotiation) {
                    json(Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                        prettyPrint = false
                    })
                }
            }
        }
    }
}

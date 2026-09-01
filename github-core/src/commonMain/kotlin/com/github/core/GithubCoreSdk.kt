package com.github.core

import com.github.core.apm.TraceTimer
import com.github.core.cache.GithubCache
import com.github.core.network.GithubNetworkClient

class GithubCoreSdk(
    val networkClient: GithubNetworkClient = GithubNetworkClient(),
    val cache: GithubCache = GithubCache()
) {
    companion object {
        fun create(): GithubCoreSdk = GithubCoreSdk()
    }
}

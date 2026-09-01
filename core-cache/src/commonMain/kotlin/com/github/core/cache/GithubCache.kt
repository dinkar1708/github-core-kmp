package com.github.core.cache

import com.github.core.domain.model.Repository

class GithubCache {
    private val memoryStore = mutableMapOf<String, List<Repository>>()

    fun save(query: String, repositories: List<Repository>) {
        memoryStore[query] = repositories
    }

    fun get(query: String): List<Repository>? {
        return memoryStore[query]
    }

    fun clear() {
        memoryStore.clear()
    }
}

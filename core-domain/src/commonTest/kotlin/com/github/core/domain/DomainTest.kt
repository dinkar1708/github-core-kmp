package com.github.core.domain

import com.github.core.domain.model.Owner
import com.github.core.domain.model.Repository
import kotlin.test.Test
import kotlin.test.assertEquals

class DomainTest {
    @Test
    fun testRepositoryModelCreation() {
        println("🧪 [core-domain] Testing Domain Model: Repository entity instantiation...")
        val repo = Repository(
            id = 1L,
            name = "github-core-kmp",
            fullName = "dinkar1708/github-core-kmp",
            description = "Headless Multiplatform Engine",
            stargazersCount = 100,
            forksCount = 10,
            language = "Kotlin",
            htmlUrl = "https://github.com/dinkar1708/github-core-kmp",
            owner = Owner(id = 1L, login = "dinkar1708", avatarUrl = "https://avatars.githubusercontent.com/u/1")
        )
        println("✅ [core-domain] Verified Repository entity: ${repo.fullName} (Stars: ${repo.stargazersCount}, Language: ${repo.language})")
        assertEquals("github-core-kmp", repo.name)
    }
}

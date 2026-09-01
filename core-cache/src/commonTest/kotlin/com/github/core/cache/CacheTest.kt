package com.github.core.cache

import com.github.core.domain.model.Owner
import com.github.core.domain.model.Repository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CacheTest {
    @Test
    fun testCacheSaveAndRetrieve() {
        println("🧪 [core-cache] Testing Local SQLite / In-Memory Cache read/write operations...")
        val cache = GithubCache()
        
        println("🔍 [core-cache] Checking cache miss for query: 'kotlin-multiplatform'...")
        assertNull(cache.get("kotlin-multiplatform"))
        println("✅ [core-cache] Cache miss confirmed (null)")

        val mockRepos = listOf(
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

        println("💾 [core-cache] Saving ${mockRepos.size} repositories for query: 'kotlin-multiplatform'...")
        cache.save("kotlin-multiplatform", mockRepos)

        val cachedResult = cache.get("kotlin-multiplatform")
        println("📦 [core-cache] Retrieved cached data: ${cachedResult?.size} items found")
        assertEquals(1, cachedResult?.size)
        println("✅ [core-cache] Cache hit verified: ${cachedResult?.first()?.fullName}")
    }
}

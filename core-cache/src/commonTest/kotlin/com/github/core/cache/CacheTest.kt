package com.github.core.cache

import com.github.core.domain.model.Owner
import com.github.core.domain.model.Repository
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CacheTest {

    private fun createSampleRepo(id: Long, name: String) = Repository(
        id = id,
        name = name,
        fullName = "dinkar1708/$name",
        description = "Headless Multiplatform Engine",
        stargazersCount = 500,
        forksCount = 50,
        language = "Kotlin",
        htmlUrl = "https://github.com/dinkar1708/$name",
        owner = Owner(id = 1L, login = "dinkar1708", avatarUrl = "https://avatars.githubusercontent.com/u/1")
    )

    @Test
    fun testCacheSaveAndRetrieve() = runTest {
        println("🧪 [core-cache] Testing Thread-Safe In-Memory Cache read/write operations...")
        val cache = GithubCache()

        println("🔍 [core-cache] Checking cache miss for query: 'kotlin-multiplatform'...")
        assertNull(cache.get("kotlin-multiplatform"))
        println("✅ [core-cache] Cache miss confirmed (null)")

        val mockRepos = listOf(createSampleRepo(101L, "github-core-kmp"))

        println("💾 [core-cache] Saving ${mockRepos.size} repositories for query: 'kotlin-multiplatform'...")
        cache.save("kotlin-multiplatform", mockRepos)

        val cachedResult = cache.get("kotlin-multiplatform")
        println("📦 [core-cache] Retrieved cached data: ${cachedResult?.size} items found")
        assertEquals(1, cachedResult?.size)
        assertEquals("github-core-kmp", cachedResult?.first()?.name)
        assertTrue(cache.isFresh("kotlin-multiplatform"))
        println("✅ [core-cache] Cache hit verified: ${cachedResult?.first()?.fullName}")
    }

    @Test
    fun testKeyNormalizationCaseInsensitive() = runTest {
        println("🧪 [core-cache] Testing key trimming and case insensitivity...")
        val cache = GithubCache()
        val mockRepos = listOf(createSampleRepo(102L, "kotlin-repo"))

        cache.save("  Kotlin  ", mockRepos)
        val hit = cache.get("kotlin")
        assertEquals(1, hit?.size)
        println("✅ [core-cache] Key normalized successfully")
    }

    @Test
    fun testCacheExpirationAfterTtl() = runTest {
        println("🧪 [core-cache] Testing TTL expiration with mock clock...")
        var currentEpochMs = 1_000_000L
        val mockClock = object : Clock {
            override fun now(): Instant = Instant.fromEpochMilliseconds(currentEpochMs)
        }

        val ttlMs = 1000L
        val cache = GithubCache(ttlMillis = ttlMs, clock = mockClock)
        cache.save("query", listOf(createSampleRepo(103L, "repo-ttl")))

        assertTrue(cache.isFresh("query"))
        assertEquals(1, cache.get("query")?.size)

        // Advance clock past TTL
        currentEpochMs += 1001L
        assertFalse(cache.isFresh("query"))
        assertNull(cache.get("query"), "Expired cache entry should return null and be purged")
        println("✅ [core-cache] Cache entry expired and purged successfully after TTL")
    }

    @Test
    fun testLruEvictionAtMaxCapacity() = runTest {
        println("🧪 [core-cache] Testing capacity bounds and eviction...")
        val cache = GithubCache(maxCapacity = 2)

        cache.save("q1", listOf(createSampleRepo(1L, "repo-1")))
        cache.save("q2", listOf(createSampleRepo(2L, "repo-2")))
        assertEquals(2, cache.size())

        // Saving 3rd entry evicts oldest (q1)
        cache.save("q3", listOf(createSampleRepo(3L, "repo-3")))
        assertEquals(2, cache.size())
        assertNull(cache.get("q1"), "Oldest entry q1 should have been evicted")
        assertEquals("repo-2", cache.get("q2")?.first()?.name)
        assertEquals("repo-3", cache.get("q3")?.first()?.name)
        println("✅ [core-cache] Capacity eviction verified")
    }
}

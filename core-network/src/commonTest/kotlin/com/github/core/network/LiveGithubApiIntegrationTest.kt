package com.github.core.network

import com.github.core.domain.model.SortField
import com.github.core.domain.model.SortOrder
import kotlinx.coroutines.test.runTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertTrue

class LiveGithubApiIntegrationTest {

    /**
     * 🌐 LIVE GITHUB API INTEGRATION TEST
     *
     * Sends an ACTUAL LIVE HTTP request over the internet to GitHub's REST API:
     * GET https://api.github.com/search/repositories?q=kotlin&sort=stars&order=desc&page=1&per_page=3
     *
     * ⚠️ NOTE:
     * Keep `@Ignore` enabled so offline builds and CI/CD pipelines run fast and do not hit
     * GitHub rate limits.
     *
     * To run this live test against real GitHub servers on your Mac, run:
     * `./gradlew :core-network:testAndroidHostTest --rerun-tasks`
     */
    @Ignore // <-- Keep enabled for CI/CD. Run via :core-network:testAndroidHostTest
    @Test
    fun testLiveGithubSearchApiCall() = runTest {
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("📡 [LIVE TEST] Sending real HTTP request to api.github.com...")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

        val client = GithubNetworkClient()
        val result = client.apiService.searchRepositories(
            query = "kotlin",
            sortField = SortField.STARS,
            sortOrder = SortOrder.DESC,
            page = 1,
            perPage = 3
        )

        assertTrue(result.isSuccess, "Live API call should succeed: ${result.exceptionOrNull()?.message}")
        val searchResult = result.getOrNull()
        
        println("✅ [LIVE API SUCCESS] Total Repositories Matching 'kotlin': ${searchResult?.totalCount}")
        println("📦 Received Top 3 Repositories from GitHub Servers:")
        
        searchResult?.items?.forEachIndexed { index, repo ->
            println("   ${index + 1}. ⭐ ${repo.stargazersCount.toString().padStart(6)} | ${repo.fullName}")
            println("      🔗 URL: ${repo.htmlUrl}")
            println("      📝 Description: ${repo.description?.take(70)}...")
        }
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

        assertTrue((searchResult?.items?.size ?: 0) > 0)
    }
}

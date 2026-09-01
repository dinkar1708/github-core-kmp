package com.github.core.network

import com.github.core.network.resilience.RateLimitTracker
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class RateLimitTrackerTest {

    @Test
    fun testUpdateAndTrackRateLimitHeaders() = runTest {
        println("🧪 [core-network] Testing RateLimitTracker: header parsing & quota tracking...")
        val tracker = RateLimitTracker()

        val headers = headersOf(
            Pair("x-ratelimit-limit", listOf("60")),
            Pair("x-ratelimit-remaining", listOf("45")),
            Pair("x-ratelimit-reset", listOf("1800000000")),
            Pair("x-ratelimit-used", listOf("15")),
            Pair("x-ratelimit-resource", listOf("search"))
        )

        tracker.updateFromHeaders(headers)
        val status = tracker.currentStatus

        assertEquals(60, status?.limit)
        assertEquals(45, status?.remaining)
        assertEquals("search", status?.resource)
        assertFalse(tracker.isRateLimited())
        println("✅ [core-network] RateLimitTracker parsed headers correctly (Remaining: 45/60)")
    }
}

package com.github.core.network

import com.github.core.domain.error.DomainError
import com.github.core.network.resilience.RetryPolicy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RetryPolicyTest {

    @Test
    fun testImmediateSuccessRequiresNoRetries() = runTest {
        println("🧪 [core-network] Testing RetryPolicy: single immediate success...")
        var executionCount = 0
        val policy = RetryPolicy(maxRetries = 3, delayFn = {})

        val result = policy.execute("test-immediate") { attempt ->
            executionCount++
            Result.success("Success on attempt $attempt")
        }

        assertTrue(result.isSuccess)
        assertEquals(1, executionCount)
        println("✅ [core-network] Succeeded on first attempt with 0 retries")
    }

    @Test
    fun testRetryOnTransientErrorSucceedsOnAttempt2() = runTest {
        println("🧪 [core-network] Testing RetryPolicy: retry on 503 error...")
        var executionCount = 0
        val policy = RetryPolicy(maxRetries = 3, delayFn = {})

        val result = policy.execute("test-transient") { attempt ->
            executionCount++
            if (attempt == 1) {
                Result.failure(DomainError.NetworkError("503 Service Unavailable", statusCode = 503))
            } else {
                Result.success("Recovered on attempt $attempt")
            }
        }

        assertTrue(result.isSuccess)
        assertEquals(2, executionCount)
        println("✅ [core-network] Succeeded on attempt 2 after transient 503 error")
    }

    @Test
    fun testNonRetriableClientErrorFailsImmediately() = runTest {
        println("🧪 [core-network] Testing RetryPolicy: non-retriable 404 error...")
        var executionCount = 0
        val policy = RetryPolicy(maxRetries = 3, delayFn = {})

        val result = policy.execute("test-404") { attempt ->
            executionCount++
            Result.failure<String>(DomainError.NetworkError("404 Not Found", statusCode = 404))
        }

        assertTrue(result.isFailure)
        assertEquals(1, executionCount, "404 Not Found should NOT be retried")
        println("✅ [core-network] 404 Not Found failed fast without wasteful retries")
    }
}

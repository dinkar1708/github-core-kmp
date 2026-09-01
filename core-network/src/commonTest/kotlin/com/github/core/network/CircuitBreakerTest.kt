package com.github.core.network

import com.github.core.domain.error.DomainError
import com.github.core.network.resilience.CircuitBreaker
import com.github.core.network.resilience.CircuitState
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CircuitBreakerTest {

    @Test
    fun testClosedCircuitNormalOperation() = runTest {
        println("🧪 [core-network] Testing CircuitBreaker: normal operation in CLOSED state...")
        val breaker = CircuitBreaker(failureThreshold = 3, resetTimeoutMs = 1000L)

        assertTrue(breaker.canExecute())
        val result = breaker.execute { Result.success("OK") }
        
        assertTrue(result.isSuccess)
        assertEquals("OK", result.getOrNull())
        assertEquals(CircuitState.CLOSED, breaker.getState())
        assertEquals(0, breaker.failureCount)
        println("✅ [core-network] CircuitBreaker executed and stayed CLOSED")
    }

    @Test
    fun testCircuitTripsToOpenAfterThresholdAndFastFails() = runTest {
        println("🧪 [core-network] Testing CircuitBreaker: tripping to OPEN and fast-failing...")
        val breaker = CircuitBreaker(failureThreshold = 2, resetTimeoutMs = 1000L)

        // Failure 1 (Server Error 503)
        breaker.execute<String> { Result.failure(DomainError.NetworkError("Service Unavailable", statusCode = 503)) }
        assertEquals(CircuitState.CLOSED, breaker.getState())
        assertEquals(1, breaker.failureCount)

        // Failure 2 (Server Error 500 -> Threshold reached!)
        breaker.execute<String> { Result.failure(DomainError.NetworkError("Internal Error", statusCode = 500)) }
        assertEquals(CircuitState.OPEN, breaker.getState())
        assertFalse(breaker.canExecute())
        println("✅ [core-network] CircuitBreaker correctly tripped to OPEN state after 2 failures")

        // Fast-fail attempt while OPEN without executing network block
        var blockExecuted = false
        val fastFailResult = breaker.execute<String> {
            blockExecuted = true
            Result.success("Should not run")
        }

        assertTrue(fastFailResult.isFailure)
        assertFalse(blockExecuted, "Block should not execute when Circuit is OPEN")
        println("✅ [core-network] CircuitBreaker fast-failed request while in OPEN state")
    }

    @Test
    fun testHalfOpenCanaryProbingRecoversToClosed() = runTest {
        println("🧪 [core-network] Testing CircuitBreaker: HALF_OPEN canary probing with threshold 2...")
        val breaker = CircuitBreaker(
            failureThreshold = 1,
            resetTimeoutMs = 0L, // Immediate timeout for test
            halfOpenSuccessThreshold = 2
        )

        // 1 Failure -> OPEN
        breaker.execute<String> { Result.failure(DomainError.NetworkError("Fail", statusCode = 500)) }
        
        // Next check transitions to HALF_OPEN (timeout 0ms elapsed)
        assertTrue(breaker.canExecute())
        assertEquals(CircuitState.HALF_OPEN, breaker.getState())
        println("✅ [core-network] Transitioned to HALF_OPEN")

        // Canary Success 1/2 -> Still HALF_OPEN
        breaker.execute { Result.success("Canary 1") }
        assertEquals(CircuitState.HALF_OPEN, breaker.getState())
        assertEquals(1, breaker.halfOpenSuccessCount)

        // Canary Success 2/2 -> Reached threshold, back to CLOSED
        breaker.execute { Result.success("Canary 2") }
        assertEquals(CircuitState.CLOSED, breaker.getState())
        assertEquals(0, breaker.failureCount)
        println("✅ [core-network] CircuitBreaker fully recovered to CLOSED after 2 canary successes")
    }
}

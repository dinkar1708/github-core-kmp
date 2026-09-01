package com.github.core.network.resilience

import com.github.core.domain.error.DomainError
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock

/**
 * Circuit Breaker state machine for Kotlin Multiplatform:
 * - CLOSED: Normal operating state. Requests pass through.
 * - OPEN: Downstream service is failing. Requests fail-fast without hitting network.
 * - HALF_OPEN: Trial state after timeout. Canary probe requests test backend recovery.
 */
enum class CircuitState {
    CLOSED,
    OPEN,
    HALF_OPEN
}

class CircuitBreaker(
    val failureThreshold: Int = DEFAULT_FAILURE_THRESHOLD,
    val resetTimeoutMs: Long = DEFAULT_RESET_TIMEOUT_MS,
    val halfOpenSuccessThreshold: Int = DEFAULT_HALF_OPEN_SUCCESS_THRESHOLD,
    private val clock: Clock = Clock.System
) {
    private val mutex = Mutex()

    var state: CircuitState = CircuitState.CLOSED
        private set

    var failureCount: Int = 0
        private set

    var halfOpenSuccessCount: Int = 0
        private set

    var lastStateChangeTime: Long = clock.now().toEpochMilliseconds()
        private set

    /**
     * Check if request execution is permitted by the circuit breaker.
     * Evaluates and transitions from OPEN -> HALF_OPEN when resetTimeoutMs has elapsed.
     */
    suspend fun canExecute(): Boolean {
        return mutex.withLock {
            val now = clock.now().toEpochMilliseconds()
            when (state) {
                CircuitState.CLOSED -> true
                CircuitState.OPEN -> {
                    val timeInOpen = now - lastStateChangeTime
                    if (timeInOpen >= resetTimeoutMs) {
                        state = CircuitState.HALF_OPEN
                        lastStateChangeTime = now
                        halfOpenSuccessCount = 0
                        println("⚡ [CircuitBreaker] Timeout elapsed (${timeInOpen}ms). Transitioned: OPEN -> HALF_OPEN (Canary Probing)")
                        true
                    } else {
                        false
                    }
                }
                CircuitState.HALF_OPEN -> true
            }
        }
    }

    /**
     * Record a successful network request.
     */
    suspend fun recordSuccess() {
        mutex.withLock {
            val now = clock.now().toEpochMilliseconds()
            when (state) {
                CircuitState.HALF_OPEN -> {
                    halfOpenSuccessCount++
                    println("📊 [CircuitBreaker] Canary probe success ($halfOpenSuccessCount/$halfOpenSuccessThreshold)")
                    if (halfOpenSuccessCount >= halfOpenSuccessThreshold) {
                        state = CircuitState.CLOSED
                        failureCount = 0
                        lastStateChangeTime = now
                        println("✅ [CircuitBreaker] Service recovered! Transitioned: HALF_OPEN -> CLOSED")
                    }
                }
                CircuitState.CLOSED -> {
                    failureCount = 0
                }
                CircuitState.OPEN -> {
                    // No action in OPEN state without timeout transition
                }
            }
        }
    }

    /**
     * Record a network / server failure.
     */
    suspend fun recordFailure(error: Throwable? = null) {
        val isServerOrNetworkError = error == null || (error is DomainError.NetworkError && (error.statusCode == null || error.statusCode in 500..599))
        if (!isServerOrNetworkError) return

        mutex.withLock {
            val now = clock.now().toEpochMilliseconds()
            when (state) {
                CircuitState.CLOSED -> {
                    failureCount++
                    println("⚠️ [CircuitBreaker] Recorded failure ($failureCount/$failureThreshold): ${error?.message}")
                    if (failureCount >= failureThreshold) {
                        state = CircuitState.OPEN
                        lastStateChangeTime = now
                        println("🚨 [CircuitBreaker] Failure threshold reached! Transitioned: CLOSED -> OPEN")
                    }
                }
                CircuitState.HALF_OPEN -> {
                    // Immediate trip back to OPEN on canary probe failure
                    state = CircuitState.OPEN
                    lastStateChangeTime = now
                    halfOpenSuccessCount = 0
                    println("🚨 [CircuitBreaker] Canary probe request failed! Transitioned: HALF_OPEN -> OPEN")
                }
                CircuitState.OPEN -> {
                    lastStateChangeTime = now
                }
            }
        }
    }

    /**
     * Execute a suspending block protected by the circuit breaker.
     */
    suspend fun <T> execute(block: suspend () -> Result<T>): Result<T> {
        if (!canExecute()) {
            val remainingMs = mutex.withLock {
                (resetTimeoutMs - (clock.now().toEpochMilliseconds() - lastStateChangeTime)).coerceAtLeast(0)
            }
            return Result.failure(
                DomainError.NetworkError(
                    message = "Circuit breaker is OPEN. Fast-failing request to protect system. Retry in ${remainingMs}ms.",
                    statusCode = 503
                )
            )
        }

        val result = block()

        if (result.isSuccess) {
            recordSuccess()
        } else {
            recordFailure(result.exceptionOrNull())
        }

        return result
    }

    /**
     * Current state of the circuit breaker with automatic timeout evaluation.
     */
    suspend fun getState(): CircuitState {
        return mutex.withLock {
            val now = clock.now().toEpochMilliseconds()
            if (state == CircuitState.OPEN && now - lastStateChangeTime >= resetTimeoutMs) {
                state = CircuitState.HALF_OPEN
                lastStateChangeTime = now
                halfOpenSuccessCount = 0
            }
            state
        }
    }

    /**
     * Reset circuit breaker to initial CLOSED state (for testing or manual recovery).
     */
    suspend fun reset() {
        mutex.withLock {
            state = CircuitState.CLOSED
            failureCount = 0
            halfOpenSuccessCount = 0
            lastStateChangeTime = clock.now().toEpochMilliseconds()
        }
    }

    companion object {
        const val DEFAULT_FAILURE_THRESHOLD = 5
        const val DEFAULT_RESET_TIMEOUT_MS = 60_000L
        const val DEFAULT_HALF_OPEN_SUCCESS_THRESHOLD = 2
    }
}

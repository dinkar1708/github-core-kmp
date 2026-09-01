package com.github.core.network.resilience

import com.github.core.domain.error.DomainError
import kotlinx.coroutines.delay

class RetryPolicy(
    val maxRetries: Int = 3,
    val initialDelayMs: Long = 300L,
    val maxDelayMs: Long = 3000L,
    val backoffMultiplier: Double = 2.0,
    private val delayFn: suspend (Long) -> Unit = { delay(it) }
) {
    suspend fun <T> execute(
        operationName: String = "HTTP Request",
        block: suspend (attempt: Int) -> Result<T>
    ): Result<T> {
        var currentDelay = initialDelayMs

        for (attempt in 1..maxRetries) {
            val result = block(attempt)

            if (result.isSuccess) {
                if (attempt > 1) {
                    println("✅ [RetryPolicy] '$operationName' succeeded on retry attempt $attempt/$maxRetries")
                }
                return result
            }

            val error = result.exceptionOrNull()
            if (!isRetriable(error) || attempt == maxRetries) {
                if (attempt > 1) {
                    println("🛑 [RetryPolicy] '$operationName' exhausted all $maxRetries retry attempts: ${error?.message}")
                }
                return result
            }

            println("⚠️ [RetryPolicy] '$operationName' failed on attempt $attempt/$maxRetries (${error?.message}). Retrying in ${currentDelay}ms...")
            delayFn(currentDelay)
            currentDelay = (currentDelay * backoffMultiplier).toLong().coerceAtMost(maxDelayMs)
        }

        return block(maxRetries)
    }

    fun isRetriable(error: Throwable?): Boolean {
        if (error == null) return false
        return when (error) {
            is DomainError.ValidationError -> false
            is DomainError.NotFoundError -> false
            is DomainError.NetworkError -> {
                val code = error.statusCode
                // 4xx client errors (except 429 Too Many Requests or 408 Timeout) are not retriable
                if (code != null && code in 400..499 && code != 408 && code != 429) {
                    false
                } else {
                    true // 5xx server errors, timeouts, network drops are retriable
                }
            }
            else -> true
        }
    }
}

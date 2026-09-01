package com.github.core.network.resilience

import io.ktor.http.Headers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock

data class RateLimitInfo(
    val limit: Int,
    val remaining: Int,
    val resetEpochSeconds: Long,
    val resource: String = "core",
    val used: Int = 0
) {
    val isExhausted: Boolean
        get() = remaining <= 0 && resetEpochSeconds > Clock.System.now().epochSeconds

    val secondsUntilReset: Long
        get() = (resetEpochSeconds - Clock.System.now().epochSeconds).coerceAtLeast(0)
}

class RateLimitTracker {
    private val mutex = Mutex()

    var currentStatus: RateLimitInfo? = null
        private set

    suspend fun updateFromHeaders(headers: Headers) {
        val limit = headers["x-ratelimit-limit"]?.toIntOrNull()
        val remaining = headers["x-ratelimit-remaining"]?.toIntOrNull()
        val reset = headers["x-ratelimit-reset"]?.toLongOrNull()
        val resource = headers["x-ratelimit-resource"] ?: "core"
        val used = headers["x-ratelimit-used"]?.toIntOrNull() ?: 0

        if (limit != null && remaining != null && reset != null) {
            mutex.withLock {
                currentStatus = RateLimitInfo(
                    limit = limit,
                    remaining = remaining,
                    resetEpochSeconds = reset,
                    resource = resource,
                    used = used
                )
            }
        }
    }

    suspend fun isRateLimited(): Boolean {
        return mutex.withLock {
            currentStatus?.isExhausted ?: false
        }
    }
}

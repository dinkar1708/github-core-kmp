package com.github.core.apm

import kotlin.time.Duration
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * High-precision multiplatform execution timer for APM observability.
 * Employs monotonic time measurement to prevent negative durations from system clock jumps.
 */
class TraceTimer(
    val name: String,
    private val timeSource: TimeSource = TimeSource.Monotonic
) {
    private var startMark: TimeMark? = null
    var durationMs: Long = 0L
        private set
    var durationMicroseconds: Long = 0L
        private set

    fun start(): TraceTimer {
        startMark = timeSource.markNow()
        return this
    }

    fun stop(): Long {
        val mark = startMark ?: timeSource.markNow().also { startMark = it }
        val elapsed: Duration = mark.elapsedNow()
        durationMs = elapsed.inWholeMilliseconds
        durationMicroseconds = elapsed.inWholeMicroseconds
        return durationMs
    }

    companion object {
        /**
         * Inline utility to measure execution time of a code block.
         */
        inline fun <T> measure(
            name: String,
            timeSource: TimeSource = TimeSource.Monotonic,
            block: () -> T
        ): Pair<T, Long> {
            val timer = TraceTimer(name, timeSource).start()
            val result = block()
            val duration = timer.stop()
            return result to duration
        }
    }
}

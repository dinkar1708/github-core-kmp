package com.github.core.apm

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApmTest {

    @Test
    fun testTraceTimerMeasurement() {
        println("🧪 [core-apm] Testing APM TraceTimer span & duration measurement...")
        val timer = TraceTimer("github_search_api")

        println("⏱️ [core-apm] Starting trace timer: '${timer.name}'...")
        timer.start()

        // Perform brief busy work
        var acc = 0L
        for (i in 1..10_000) {
            acc += i
        }

        val durationMs = timer.stop()
        println("📊 [core-apm] Stopped trace timer: '${timer.name}', recorded duration: ${durationMs}ms (${timer.durationMicroseconds}µs)")
        assertTrue(durationMs >= 0L, "Duration must be non-negative")
        assertEquals(durationMs, timer.durationMs)
        assertTrue(timer.durationMicroseconds >= 0L)
        println("✅ [core-apm] APM Telemetry metric successfully recorded")
    }

    @Test
    fun testMeasureInlineHelper() {
        println("🧪 [core-apm] Testing TraceTimer.measure inline block...")
        val (result, durationMs) = TraceTimer.measure("inline_operation") {
            "computed_value"
        }

        assertEquals("computed_value", result)
        assertTrue(durationMs >= 0L)
        println("✅ [core-apm] Inline measurement completed with duration: ${durationMs}ms")
    }
}

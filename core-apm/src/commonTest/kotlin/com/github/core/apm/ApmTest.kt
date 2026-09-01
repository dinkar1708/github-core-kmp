package com.github.core.apm

import kotlin.test.Test
import kotlin.test.assertEquals

class ApmTest {
    @Test
    fun testTraceTimerMeasurement() {
        println("🧪 [core-apm] Testing APM TraceTimer span & duration measurement...")
        val timer = TraceTimer("github_search_api")
        
        println("⏱️ [core-apm] Starting trace timer: '${timer.name}'...")
        timer.start()
        
        val durationMs = timer.stop()
        println("📊 [core-apm] Stopped trace timer: '${timer.name}', recorded duration: ${durationMs}ms")
        assertEquals(10L, durationMs)
        println("✅ [core-apm] APM Telemetry metric successfully recorded")
    }
}

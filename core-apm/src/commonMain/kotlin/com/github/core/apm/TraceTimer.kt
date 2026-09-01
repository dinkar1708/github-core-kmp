package com.github.core.apm

class TraceTimer(val name: String) {
    private var startTime: Long = 0
    private var duration: Long = 0

    fun start() {
        startTime = 0L // Placeholder
    }

    fun stop(): Long {
        duration = 10L // Placeholder
        return duration
    }
}

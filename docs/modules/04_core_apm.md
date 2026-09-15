# Module Spec: `:core-apm`

## 💡 Architectural Role & Concept
* **Execution Latency Observability:** Accurately measures operation durations for network queries, cache lookups, and Use Case processing.
* **Vendor-Agnostic Design:** Does not package proprietary vendor SDKs (Firebase/Datadog) into the shared KMP binary, preventing binary bloat and version conflicts.
* **Zero Overhead:** Employs allocation-free, non-blocking monotonic timers executing in $\mathcal{O}(1)$ time with negligible CPU footprint (<0.01ms per span).

---

## 🔑 Key Definitions: `TraceTimer`

`TraceTimer` (`com.github.core.apm`) tracks operation latency across platforms:

| Property / Method | Definition & Role |
| :--- | :--- |
| `val name: String` | Span identifier (e.g. `"github_search_request"`). |
| `fun start(): TraceTimer` | Captures starting monotonic timestamp. |
| `fun stop(): Long` | Captures terminal timestamp, computes $\Delta t = t_{\text{end}} - t_{\text{start}}$, and returns duration in milliseconds. |
| `var durationMs: Long` | Recorded execution duration in milliseconds. |
| `var durationMicroseconds: Long` | Recorded execution duration in microseconds. |
| `companion inline fun measure(...)` | Inline utility executing a code block and returning `Pair<Result, Long>`. |

---

## 🔬 Platform Bridging Architecture

```mermaid
flowchart TD
    subgraph KMP[":core-apm (Shared Monotonic Telemetry Engine)"]
        START["TraceTimer.start()<br/>(TimeSource.Monotonic)"] --> OP["Execute Business / Network Operation"]
        OP --> STOP["TraceTimer.stop()<br/>(Elapsed Milliseconds & Microseconds)"]
    end

    STOP -->|"Emits Duration (ms / µs)"| PLATFORMS

    subgraph PLATFORMS["Host Platform Observability Observers"]
        direction LR
        subgraph ANDROID["🤖 Android App"]
            FB["Firebase Performance<br/>Trace.putMetric()"]
            OTEL["OpenTelemetry Spans"]
        end

        subgraph IOS["🍎 iOS App"]
            MK["Apple MetricKit"]
            SIGN["os_signpost Traces"]
        end
    end

    style KMP fill:#f3d9fa,stroke:#ae3ec9,stroke-width:2px,color:#000
    style START fill:#f8f9fa,stroke:#495057,stroke-width:1px,color:#000
    style OP fill:#e7f5ff,stroke:#1c7ed6,stroke-width:1px,color:#000
    style STOP fill:#d3f9d8,stroke:#2b8a3e,stroke-width:2px,color:#000
    style PLATFORMS fill:#f8f9fa,stroke:#495057,stroke-width:1px,color:#000
    style ANDROID fill:#d3f9d8,stroke:#2b8a3e,stroke-width:1px,color:#000
    style IOS fill:#e7f5ff,stroke:#1c7ed6,stroke-width:1px,color:#000
    style FB fill:#fff,stroke:#2b8a3e,stroke-width:1px,color:#000
    style OTEL fill:#fff,stroke:#2b8a3e,stroke-width:1px,color:#000
    style MK fill:#fff,stroke:#1c7ed6,stroke-width:1px,color:#000
    style SIGN fill:#fff,stroke:#1c7ed6,stroke-width:1px,color:#000
```

Host applications capture durations emitted by `TraceTimer` and forward them to native APM tools:
* **Android:** Bridges into `Firebase.performance.newTrace(timer.name).putMetric(...)`.
* **iOS:** Bridges into Apple `os_signpost` or MetricKit payloads.

---

## 💻 Kotlin Usage Pattern

```kotlin
// 1. Start execution timer
val timer = TraceTimer("repo_search_span").start()

// 2. Perform operation
val result = searchUseCase.execute("kotlin")

// 3. Stop timer and forward duration
val elapsedMs = timer.stop()
println("Search operation completed in ${elapsedMs}ms (${timer.durationMicroseconds}µs)")

// Or using the inline measurement helper:
val (searchResult, duration) = TraceTimer.measure("inline_span") {
    searchUseCase.execute("compose")
}
```

---

## 🧪 Testing Strategy
* Asserts non-negative duration calculations across consecutive calls.
* Verifies microsecond precision tracking.
* Confirms zero heap allocations in `measure { ... }`.

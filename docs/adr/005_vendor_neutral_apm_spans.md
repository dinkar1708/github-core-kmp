# ADR 005: Monotonic TraceTimer vs. Third-Party APM SDKs

## 📌 Status
`Accepted` (2026-09)

## 🎯 Context
Mobile teams require observability to track network latency, cache hit efficiency, and query durations in production.

However, embedding proprietary third-party APM SDKs (e.g. Firebase Performance, Datadog, Sentry, Dynatrace) directly into a core KMP shared library introduces severe drawbacks:
1. **Binary Bloat & Transitive Dependencies:** Forces heavy vendor SDKs onto all client applications regardless of their observability stack.
2. **Version Clashes:** Triggers transitive dependency conflicts with the consumer application's own version of Firebase or Datadog.
3. **Inaccurate Time Measurements:** Using `System.currentTimeMillis()` can produce negative or inflated durations if NTP clock synchronization jumps during a request.

## 💡 Decision
We created a lightweight, zero-dependency APM module [**`:core-apm`**](../../core-apm/README.md) using Kotlin's standard library `TimeSource.Monotonic`:
1. **Monotonic High Precision:** [`TraceTimer`](../../core-apm/src/commonMain/kotlin/com/github/core/apm/TraceTimer.kt) employs `TimeSource.Monotonic.markNow()` to measure elapsed time at microsecond precision, completely immune to wall-clock adjustments or daylight saving jumps.
2. **Inline Execution Helper:** Provides an allocation-free `measure(name) { ... }` inline lambda returning both the execution result and measured duration in whole milliseconds.
3. **Pluggable Telemetry:** Exposes measured duration spans to the consumer application so the client app can forward metrics to their preferred backend (Datadog, Firebase, MetricKit, or custom telemetry) without the SDK dictating the vendor.

```mermaid
flowchart LR
    KMP[":core-apm<br/>TraceTimer(name)<br/>(TimeSource.Monotonic)"]
    SPAN["Duration Span<br/>(ms / µs)"]

    FB["Firebase Performance"]
    DD["Datadog RUM / Traces"]
    MK["Apple MetricKit"]
    OTEL["OpenTelemetry"]

    KMP --> SPAN
    SPAN -.->|Forwarded by Android App| FB
    SPAN -.->|Forwarded by Android App| DD
    SPAN -.->|Forwarded by iOS App| MK
    SPAN -.->|Forwarded by Backend/Client| OTEL

    style KMP fill:#f3d9fa,stroke:#ae3ec9,stroke-width:2px,color:#000
    style SPAN fill:#f8f9fa,stroke:#495057,stroke-width:1px,color:#000
    style FB fill:#d3f9d8,stroke:#2b8a3e,stroke-width:1px,color:#000
    style DD fill:#fff3bf,stroke:#f08c00,stroke-width:1px,color:#000
    style MK fill:#e7f5ff,stroke:#1c7ed6,stroke-width:1px,color:#000
    style OTEL fill:#ffe3e3,stroke:#e03131,stroke-width:1px,color:#000
```

## ⚖️ Consequences

### Positive
- **Zero Third-Party Dependencies:** Pure Kotlin standard library; zero risk of dependency version collisions.
- **Negligible Overhead:** Monotonic clock checks cost `<0.01ms` per execution span.
- **Vendor Agnostic:** Consumer applications remain free to route metrics to whichever APM or logging solution their platform standardizes on.

### Negative / Trade-offs
- The core SDK does not automatically upload spans to a remote dashboard; the consumer app must connect the measured timings to their telemetry pipeline.

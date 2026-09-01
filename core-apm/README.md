# 📊 `:core-apm`

The **Application Performance Monitoring (APM) & Telemetry engine** for the GitHub KMP SDK.

---

## 🎯 Architectural Responsibility & Boundary

* **Trace Timers:** High-precision execution timers tracking network latency, cache read/write speeds, and Use Case throughput.
* **Metric Dispatcher:** Batched, non-blocking dispatch of metrics, breadcrumbs, and performance events.
* **Pluggable Reporters:** Interceptor interface allowing client applications (Android, iOS, Flutter) to bridge KMP traces into Firebase Performance, Datadog, or custom telemetry backends.
* **Zero Overhead:** Minimal allocation overhead using inline trace blocks and lightweight structured events.

---

## 🏗️ Structure

```text
core-apm/
├── src/
│   ├── commonMain/kotlin/com/github/core/apm/
│   │   ├── timer/         # TraceTimer & execution measurement
│   │   ├── model/         # Metric, Event, & Span data models
│   │   ├── reporter/      # Pluggable APMReporter interfaces
│   │   └── telemetry/     # TelemetryManager & event batching
│   └── commonTest/        # APM metric recording & timer unit tests
└── build.gradle.kts
```

---

## 🧪 Running Tests

### 1. Standard Run (uses Gradle cache):
```bash
./gradlew :core-apm:allTests
```

### 2. Force Re-run & Print Live Logs (bypasses cache):
```bash
./gradlew :core-apm:allTests --rerun-tasks
```

### 3. Target-Specific Test Commands:
```bash
# Android Host / JVM telemetry tests
./gradlew :core-apm:testAndroidHostTest --rerun-tasks

# iOS Simulator telemetry tests
./gradlew :core-apm:iosSimulatorArm64Test --rerun-tasks
```

# ⏱️ Telemetry & Performance Benchmarks

This directory contains empirical performance measurements, latency comparisons, and binary size footprint analyses for **`github-core-kmp`**.

---

## 📊 Benchmark Reports

| Report | Description | Key Focus |
| :--- | :--- | :--- |
| [**01. SDK Performance & Footprint**](./01_sdk_performance_and_footprint.md) | Latency benchmarks, memory allocations, factory startup, and artifact sizes. | In-memory cache speed, API timings, `TraceTimer` overhead, AAR and XCFramework payload. |

---

## 🔬 Telemetry Highlights

* **Cache Retrieval Latency:** `< 1 ms` (Thread-safe Mutex in-memory retrieval).
* **SDK Cold Start:** `< 4 ms` (Instantaneous `GithubCoreSdk.create()` initialization).
* **`TraceTimer` Profiling Overhead:** `< 0.01 ms` per span using `TimeSource.Monotonic`.
* **Binary Size (Android AAR):** `~1.2 MB` uncompressed payload.
* **Binary Size (iOS XCFramework):** `~3.8 MB` per architecture (strips cleanly on distribution).

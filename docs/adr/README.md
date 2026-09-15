# 🏛️ Architecture Decision Records (ADRs)

This directory documents the significant architectural and technical decisions made during the design, implementation, and evolution of **`github-core-kmp`**.

The format follows [Michael Nygard's Architecture Decision Record (ADR) standard](https://cognitect.com/blog/2011/11/15/documenting-architecture-decisions).

---

## 📋 ADR Catalog

| ADR | Title | Status | Date | Decision Summary |
| :---: | :--- | :---: | :---: | :--- |
| [**001**](./001_headless_boundary_vs_shared_viewmodel.md) | **Headless Boundary vs. Shared ViewModels** | `Accepted` | 2026-09 | Stop shared code at Use Cases rather than shared ViewModels to protect native platform idioms and prevent iOS ARC memory leaks. |
| [**002**](./002_dual_engine_ktor_darwin_okhttp.md) | **Dual Platform Ktor Engines (`Darwin` & `OkHttp`)** | `Accepted` | 2026-09 | Use native HTTP engines (`Darwin` on iOS, `OkHttp` on Android) for connection pooling, background tasks, and Apple ATS compliance. |
| [**003**](./003_resilience_circuit_breaker.md) | **In-Engine Circuit Breaker & RateLimitTracker** | `Accepted` | 2026-09 | Implement a 3-state Circuit Breaker and proactive header tracker to fail-fast when the GitHub REST API is degraded or quota is exhausted. |
| [**004**](./004_in_memory_ttl_cache_eviction.md) | **Thread-Safe In-Memory TTL & LRU Caching** | `Accepted` | 2026-09 | Deploy a non-blocking coroutine `Mutex` cache with 5-minute TTL freshness and 100-entry LRU eviction to minimize network calls. |
| [**005**](./005_vendor_neutral_apm_spans.md) | **Monotonic TraceTimer vs. Third-Party APM SDKs** | `Accepted` | 2026-09 | Build a lightweight, monotonic `TraceTimer` span abstraction rather than hardcoding external vendor APM SDKs into the shared core. |

---

## 📐 ADR Lifecycle Statuses

* **`Proposed`**: Under team review and evaluation.
* **`Accepted`**: Formally adopted and implemented in the codebase.
* **`Superseded`**: Replaced by a subsequent ADR.
* **`Deprecated`**: No longer active or recommended.

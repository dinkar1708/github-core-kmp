# GitHub Core KMP (Headless Multiplatform Engine)

An enterprise-grade **Headless Kotlin Multiplatform (KMP) SDK** designed to serve as the single source of truth for **domain models, Ktor networking, validation, pagination, offline caching, and APM telemetry**.

This core engine powers three distinct native client frontends: **Jetpack Compose (Android), SwiftUI (iOS), and Flutter**, demonstrating a scalable "Mobile Enablement" platform architecture.

---

## 🏗️ Architectural Philosophy: The "Headless Boundary"

To maximize adoption across diverse product teams, this KMP engine strictly enforces a **Headless Architecture boundary**. The shared code handles the "Bottom 70%" of the app, stopping exactly at the **Domain / Use Case layer**.

### Why not share ViewModels?
Forcing Kotlin `StateFlow` or `ViewModels` onto iOS and Flutter teams often creates lifecycle memory leaks, requires heavy event bridging, and restricts engineers from using native paradigms (like `@Observable` in Swift or Riverpod in Dart). By stopping at the Use Case layer, we ensure:

1. **Zero UI Compromise:** UI teams write 100% native presentation state.
2. **True Enablement:** iOS and Flutter teams aren't forced into Kotlin paradigms; they simply consume cleanly formatted data.

```text
                      ┌──────────────────────────────────────────────┐
                      │               github-core-kmp                │
                      │       (The Shared Headless SDK Engine)       │
                      └──────────────────────┬───────────────────────┘
                                             │
        ┌────────────────────────────────────┼───────────────────────────────────┐
        ▼                                    ▼                                   ▼
┌──────────────────┐               ┌──────────────────┐                ┌──────────────────┐
│   :core-domain   │               │  :core-network   │                │   :core-cache    │
│ Pure Use Cases & │               │   Ktor Client &  │                │ SQLDelight Local │
│ Validations      │               │   Resilience     │                │ Persistence      │
└─────────┬────────┘               └─────────┬────────┘                └─────────┬────────┘
          │                                  │                                   │
          └──────────────────────────────────┼───────────────────────────────────┘
               [ Strictly Enforced Boundary: No Presentation State/ViewModels ]
             ┌───────────────────────────────┼───────────────────────────────┐
             │ (Injected via Hilt/Maven)     │ (Consumed via SKIE/SPM)       │ (Bridged via FFI/Channel)
             ▼                               ▼                               ▼
  ┌─────────────────────┐         ┌─────────────────────┐         ┌─────────────────────┐
  │  Android (Kotlin)   │         │    iOS (Swift)      │         │   Flutter (Dart)    │
  │                     │         │                     │         │                     │
  │ Native AndroidX VM  │         │ Native @MainActor VM│         │ Native Riverpod VM  │
  │ Jetpack Compose UI  │         │ SwiftUI             │         │ Flutter UI          │
  └─────────────────────┘         └─────────────────────┘         └─────────────────────┘
```

---

## 🗺️ Architectural Build Sequence (Clean Architecture Order)

In accordance with Clean Architecture principles (dependencies point strictly inwards), the SDK modules are developed and layered in the following sequential order:

```text
  1️⃣ :core-domain (Foundation Contracts)
        ▲
        ├── 2️⃣ :core-network (Remote Data Source & Ktor Client)
        │
        └── 3️⃣ :core-cache   (Local Data Source & SQLite DB)
        ▲
  4️⃣ :core-apm   (Independent Telemetry & Metric Spans)
        ▲
  5️⃣ :github-core (Public SDK Facade & Framework Assembly)
```

| Step | Module | Why in this Order? |
| :---: | :--- | :--- |
| **1️⃣** | [**:core-domain**](./core-domain/README.md) | **Core Foundation:** Establishes pure data entities, input validations, repository contracts, and Use Cases. Zero external dependencies. |
| **2️⃣** | [**:core-network**](./core-network/README.md) | **Remote Data:** Implements Ktor client, serialization, circuit breaker, retry policies, and maps network DTOs to `:core-domain` models. |
| **3️⃣** | [**:core-cache**](./core-cache/README.md) | **Offline Storage:** Implements local SQLite persistence, TTL freshness checks, LRU cache eviction, and maps cache entities to `:core-domain`. |
| **4️⃣** | [**:core-apm**](./core-apm/README.md) | **Observability:** Custom execution `TraceTimer` and batched `MetricDispatcher` to track network/cache latency across all platforms. |
| **5️⃣** | [**:github-core**](./github-core/README.md) | **Public Distribution:** Aggregates all modules into a single entrypoint (`GithubCoreSdk`) and builds `GithubCoreKMP.xcframework` (iOS) & Android AAR. |

---

## 📦 Modular Architecture

| Module | Documentation | Responsibilities | Standard Run | Force Re-run & Live Logs |
| :--- | :--- | :--- | :--- | :--- |
| **`:core-domain`** | [Read Docs](./core-domain/README.md) | Pure Kotlin entities, validations, Use Cases. Zero 3rd-party dependencies. | `./gradlew :core-domain:allTests` | `./gradlew :core-domain:allTests --rerun-tasks` |
| **`:core-network`** | [Read Docs](./core-network/README.md) | Ktor 3.x Client, serialization, GitHub API routes, Circuit Breaker, Retries. | `./gradlew :core-network:allTests` | `./gradlew :core-network:allTests --rerun-tasks` |
| **`:core-cache`** | [Read Docs](./core-cache/README.md) | Local SQLite persistence via SQLDelight, TTL freshness policies, LRU eviction. | `./gradlew :core-cache:allTests` | `./gradlew :core-cache:allTests --rerun-tasks` |
| **`:core-apm`** | [Read Docs](./core-apm/README.md) | APM `TraceTimer`, batched `MetricDispatcher`, and pluggable telemetry. | `./gradlew :core-apm:allTests` | `./gradlew :core-apm:allTests --rerun-tasks` |
| **`:github-core`** | [Read Docs](./github-core/README.md) | Umbrella SDK module producing `GithubCoreKMP.xcframework` & Android AAR. | `./gradlew :github-core:allTests` | `./gradlew :github-core:allTests --rerun-tasks` |

---

## 🛠️ Tech Stack & Tooling

* **Language:** Kotlin Multiplatform 2.x
* **Networking:** Ktor 3.x Client + `kotlinx.serialization`
* **Local Persistence:** SQLDelight / SQLite Multiplatform
* **Concurrency:** Kotlin Coroutines (`kotlinx.coroutines`)
* **Time & Dates:** `kotlinx.datetime`
* **Distribution Formats:** 
  * 🍏 **iOS:** Swift Package Manager (SPM) / `GithubCoreKMP.xcframework`
  * 🤖 **Android:** Android AAR / Maven Publication
  * 📱 **Flutter:** Platform Channel / Dart FFI Bridge
* **CI/CD:** GitHub Actions (Matrix build automating .aar and .xcframework distribution)

---

## 🔗 Consumer Client Applications

For detailed integration guides and code samples for each frontend platform, see [**TASK.md**](./TASK.md):

* 🤖 **Android Native App:** [`github-cruise-android`](https://github.com/dinkar1708/github-cruise-android)
* 🍎 **iOS Native App:** [`github-repo-search-ios`](https://github.com/dinkar1708/github-repo-search-ios)
* 📱 **Flutter App:** [`flutter_riverpod_template`](https://github.com/dinkar1708/flutter_riverpod_template)

---

## 🧪 Verification & Test Commands

### 1. Run All Tests Across All Modules
```bash
# Standard run (uses incremental cache)
./gradlew check

# Force re-run all tests and print full live console logs
./gradlew check --rerun-tasks
```

### 2. Network Testing: Mock vs. Real Live API Call

* **🛡️ Run Offline / Mock Tests (CI/CD Safe):**
  ```bash
  ./gradlew :core-network:allTests --rerun-tasks
  ```
* **📡 Run Real Live API Call (Hits `api.github.com` over the internet):**
  ```bash
  ./gradlew :core-network:testAndroidHostTest --rerun-tasks
  ```

### 3. Run Tests for an Individual Module (with Live Logs)
```bash
# 1. Pure Domain Logic Tests
./gradlew :core-domain:allTests --rerun-tasks

# 2. Network Client & MockEngine Tests
./gradlew :core-network:allTests --rerun-tasks

# 3. Cache & Database Tests
./gradlew :core-cache:allTests --rerun-tasks

# 4. APM Telemetry & Timer Tests
./gradlew :core-apm:allTests --rerun-tasks

# 5. Umbrella End-to-End SDK Tests
./gradlew :github-core:allTests --rerun-tasks
```

---

## 👤 Author

**Dinakar Prasad Maurya**

* Mobile Enablement & Platform Architect (Android · iOS · Flutter · KMP)
* Tokyo, Japan | JLPT N2
* [LinkedIn](https://linkedin.com/in/dinkar1708) · [Medium](https://medium.com/@dinkar1708) · [GitHub](https://github.com/dinkar1708)
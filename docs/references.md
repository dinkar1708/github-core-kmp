# Architecture, Platform & Technical References

## 1. Overview & Architectural Philosophy

This document compiles the catalog of authoritative technical documentation, official platform standards, and enterprise engineering publications followed throughout the architecture, design, and implementation of **`github-core-kmp`**.

`github-core-kmp` is engineered as a **Headless Kotlin Multiplatform (KMP) SDK Engine**, serving as the single source of truth for domain models, validation rules, Ktor HTTP networking, resilience pipelines, offline caching, and APM telemetry. It strictly implements JetBrains' official architectural recommendation: [**"One logic layer, native experience" (`logic-native-ui`)**](https://kotlinlang.org/multiplatform/#choose-share-what-logic-native-ui). The shared engine handles the bottom 70% of business and data logic, stopping exactly at the **Domain / Use Case layer**, while native client frontends maintain complete autonomy over UI and presentation state.

Every reference below includes its official URL and an **Application in Project** rationale detailing how it is implemented or targeted for adoption in this codebase.

---

## 2. Kotlin Multiplatform (KMP) & JetBrains Official Standards

### 2.1 Code-Sharing Paradigms & Architecture
- **JetBrains KMP Official Portal**: [https://kotlinlang.org/multiplatform/](https://kotlinlang.org/multiplatform/)  
  *Application in Project:* Primary foundational multiplatform specification for sharing Kotlin business logic across Android JVM and iOS Native Apple Darwin targets.
- **"Share logic, keep UI native" (`logic-native-ui`)**: [https://kotlinlang.org/multiplatform/#choose-share-what-logic-native-ui](https://kotlinlang.org/multiplatform/#choose-share-what-logic-native-ui)  
  *Application in Project:* The primary architectural paradigm of this SDK. Enforces the strict headless boundary stopping at the Use Case layer, preventing Kotlin presentation state (`ViewModel` / `StateFlow`) from leaking into Swift or Flutter frontends.
- **"Share a piece of logic"**: [https://kotlinlang.org/multiplatform/#choose-share-what-piece-of-logic](https://kotlinlang.org/multiplatform/#choose-share-what-piece-of-logic)  
  *Application in Project:* Implemented in [`:core-domain`](file:///Users/dinakarmaurya/Documents/Personal/github-core-kmp/core-domain) and demonstrated in [`sample/sample-share-piece-of-logic`](file:///Users/dinakarmaurya/Documents/Personal/github-core-kmp/sample/sample-share-piece-of-logic), allowing lightweight client apps to consume only input validations (`QueryValidator`, `PaginationValidator`) with zero third-party dependencies.
- **"Share both logic and UI"**: [https://kotlinlang.org/multiplatform/#choose-share-what-both-logic-ui](https://kotlinlang.org/multiplatform/#choose-share-what-both-logic-ui)  
  *Application in Project:* Explored in [`sample/sample-share-both-logic-and-ui`](file:///Users/dinakarmaurya/Documents/Personal/github-core-kmp/sample/sample-share-both-logic-and-ui), demonstrating Compose Multiplatform consuming the headless core engine.
- **KMP Project Structure & Source Sets**: [https://kotlinlang.org/docs/multiplatform-discover-project.html](https://kotlinlang.org/docs/multiplatform-discover-project.html)  
  *Application in Project:* Configures modular multiplatform source sets (`commonMain`, `androidMain`, `iosMain`, `commonTest`) across all subprojects (`:core-domain`, `:core-network`, `:core-cache`, `:core-apm`, `:github-core`).
- **Kotlin Expect and Actual Declarations**: [https://kotlinlang.org/docs/multiplatform-expect-actual.html](https://kotlinlang.org/docs/multiplatform-expect-actual.html)  
  *Application in Project:* Platform abstraction pattern used in `:core-network` via `createPlatformHttpClient()` to instantiate `OkHttp` on Android and `Darwin` on iOS without polluting `commonMain` with platform-specific imports.
- **Objective-C and Swift Interoperability**: [https://kotlinlang.org/docs/native-objc-interop.html](https://kotlinlang.org/docs/native-objc-interop.html)  
  *Application in Project:* Packaging `:github-core` as a static `GithubCoreKMP.xcframework` with exported transitives (`:core-domain`, `:core-network`, `:core-cache`, `:core-apm`) for seamless import into Swift Package Manager (SPM).
- **Kotlin Coding Conventions**: [https://kotlinlang.org/docs/coding-conventions.html](https://kotlinlang.org/docs/coding-conventions.html)  
  *Application in Project:* PascalCase naming for entities/use cases, single-responsibility files, explicit return types on public SDK APIs, and elimination of Hungarian notation.
- **Monotonic Time & High-Precision Timing**: [https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.time/-time-source/-monotonic/](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.time/-time-source/-monotonic/)  
  *Application in Project:* Used in `:core-apm` (`TraceTimer`) to record execution spans and latency with allocation-free, monotonically non-decreasing timestamps immune to system clock adjustments.

---

## 3. Software Architecture & Enterprise Design Patterns

### 3.1 Clean Architecture & Decoupling
- **Robert C. Martin: The Clean Architecture**: [https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)  
  *Application in Project:* Enforces the Dependency Inversion Principle where `:core-domain` resides at the innermost center with zero external dependencies. External layers (`:core-network`, `:core-cache`) depend strictly inwards on domain interfaces (`GithubRepository`).
- **Single Source of Truth (SSOT) & Repository Pattern**: [https://developer.android.com/topic/architecture/data-layer](https://developer.android.com/topic/architecture/data-layer)  
  *Application in Project:* `GithubRepository` acts as the single contract mediating between remote network requests and local cache entries, providing a unified `Result<T>` stream to domain Use Cases.
- **Domain Interactors / Use Cases**: [https://developer.android.com/topic/architecture/domain-layer](https://developer.android.com/topic/architecture/domain-layer)  
  *Application in Project:* Granular, reusable business interactors (`SearchRepositoriesUseCase`, `GetRepositoryDetailUseCase`, `GetUserProfileUseCase`, `GetUserRepositoriesUseCase`) isolating input validation and business logic.
- **Architecture Decision Records (ADRs - Michael Nygard)**: [https://cognitect.com/blog/2011/11/15/documenting-architecture-decisions](https://cognitect.com/blog/2011/11/15/documenting-architecture-decisions)  
  *Application in Project:* Documentation standard adopted in `docs/` to systematically record architectural trade-offs, rationale, and consequences for key technical choices.

### 3.2 Enterprise Resilience & Fault Tolerance
- **Martin Fowler: Circuit Breaker Pattern**: [https://martinfowler.com/bliki/CircuitBreaker.html](https://martinfowler.com/bliki/CircuitBreaker.html)  
  *Application in Project:* Implemented in [`CircuitBreaker.kt`](file:///Users/dinakarmaurya/Documents/Personal/github-core-kmp/core-network/src/commonMain/kotlin/com/github/core/network/resilience/CircuitBreaker.kt) with a 3-state state machine (`CLOSED`, `OPEN`, `HALF_OPEN`), failure thresholds, reset timeouts, and canary probing to protect backends from cascading failures.
- **Microsoft Azure Architecture Center: Circuit Breaker Pattern**: [https://learn.microsoft.com/en-us/azure/architecture/patterns/circuit-breaker](https://learn.microsoft.com/en-us/azure/architecture/patterns/circuit-breaker)  
  *Application in Project:* Informs error classification (tripping only on 5xx server errors and network connection drops while bypassing 4xx client errors) and fast-fail recovery semantics.
- **AWS Architecture: Exponential Backoff and Jitter**: [https://aws.amazon.com/blogs/architecture/exponential-backoff-and-jitter/](https://aws.amazon.com/blogs/architecture/exponential-backoff-and-jitter/)  
  *Application in Project:* Implemented in [`RetryPolicy.kt`](file:///Users/dinakarmaurya/Documents/Personal/github-core-kmp/core-network/src/commonMain/kotlin/com/github/core/network/resilience/RetryPolicy.kt) to execute progressive delays (`initialDelayMs * backoffMultiplier^attempt`) for idempotent network operations.
- **Google Cloud: Truncated Exponential Backoff**: [https://cloud.google.com/storage/docs/retry-strategy](https://cloud.google.com/storage/docs/retry-strategy)  
  *Application in Project:* Caps retry delays at `maxDelayMs` (3,000ms) to ensure interactive mobile users are not trapped in indefinitely expanding delay loops.

---

## 4. Networking, Serialization & Multiplatform Concurrency

### 4.1 Ktor Client 3.x Multiplatform
- **Ktor Multiplatform HTTP Client**: [https://ktor.io/docs/client-create-multiplatform-application.html](https://ktor.io/docs/client-create-multiplatform-application.html)  
  *Application in Project:* Shared HTTP client infrastructure across Android and iOS providing request configuration, header management, and interceptor pipelines.
- **Ktor Client Multiplatform Engines**: [https://ktor.io/docs/client-engines.html](https://ktor.io/docs/client-engines.html)  
  *Application in Project:* Native platform engine selection: `OkHttp` on Android for robust connection pooling and HTTP/2 multiplexing, and `Darwin` (`NSURLSession`) on iOS for Apple battery and background task optimization.
- **Ktor ContentNegotiation & JSON Serialization**: [https://ktor.io/docs/client-serialization.html](https://ktor.io/docs/client-serialization.html)  
  *Application in Project:* Automated JSON serialization/deserialization configured with `ignoreUnknownKeys = true`, `isLenient = true`, and `coerceInputValues = true` for schema tolerance.
- **Ktor Client Testing (`MockEngine`)**: [https://ktor.io/docs/client-testing.html](https://ktor.io/docs/client-testing.html)  
  *Application in Project:* High-speed, deterministic offline unit testing in [`NetworkTest.kt`](file:///Users/dinakarmaurya/Documents/Personal/github-core-kmp/core-network/src/commonTest/kotlin/com/github/core/network/NetworkTest.kt) mocking HTTP status codes, JSON payloads, and response headers.

### 4.2 Serialization & Concurrency
- **Kotlinx Serialization Guide**: [https://github.com/Kotlin/kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization)  
  *Application in Project:* Compile-time, reflectionless JSON parser generating efficient multiplatform serializers for GitHub API DTOs (`SearchRepositoriesResponseDto`, `RepositoryDto`, `UserDto`).
- **Kotlinx Coroutines Multiplatform**: [https://github.com/Kotlin/kotlinx.coroutines](https://github.com/Kotlin/kotlinx.coroutines)  
  *Application in Project:* Non-blocking asynchronous programming model across threads and native Apple runloops.
- **Kotlin Structured Concurrency**: [https://kotlinlang.org/docs/coroutines-basics.html#structured-concurrency](https://kotlinlang.org/docs/coroutines-basics.html#structured-concurrency)  
  *Application in Project:* Ensures cancellation propagates cleanly through Use Cases and resilience blocks, mandating that `CancellationException` is never caught or retried by retry handlers.
- **Kotlinx Coroutines Synchronization & Mutex**: [https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.sync/-mutex/](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.sync/-mutex/)  
  *Application in Project:* Non-blocking multiplatform mutual exclusion locks used in `CircuitBreaker`, `RateLimitTracker`, and `GithubCache` to eliminate data races across background threads.
- **Kotlinx Datetime**: [https://github.com/Kotlin/kotlinx-datetime](https://github.com/Kotlin/kotlinx-datetime)  
  *Application in Project:* Cross-platform date, time, and epoch timestamp computations for GitHub rate limit reset calculations and cache freshness expiration.

---

## 5. Security & Transport Layer (TLS / Network Security)

- **Apple App Transport Security (ATS)**: [https://developer.apple.com/documentation/security/preventing_insecure_network_connections](https://developer.apple.com/documentation/security/preventing_insecure_network_connections)  
  *Application in Project:* Dictates that iOS network connections must employ secure TLS 1.2+ with forward secrecy. Custom certificate challenge handlers must never bypass system root CA evaluation.
- **Apple Server Trust Evaluation (`SecTrustEvaluateWithError`)**: [https://developer.apple.com/documentation/security/certificate_key_and_trust_services/trust](https://developer.apple.com/documentation/security/certificate_key_and_trust_services/trust)  
  *Application in Project:* Authoritative guideline on handling `NSURLAuthenticationMethodServerTrust`. Prohibits blindly returning `NSURLCredential.credentialForTrust(trust)` without evaluation, and mandates relying on Darwin's default trust handling to protect users against Man-in-the-Middle (MitM) attacks.
- **OWASP Mobile Application Security (MASVS - Network Communication)**: [https://mas.owasp.org/MASVS/controls/MASVS-NETWORK-1/](https://mas.owasp.org/MASVS/controls/MASVS-NETWORK-1/)  
  *Application in Project:* Enterprise mobile security benchmark requiring encrypted network channels, strict certificate path validation, and prevention of sensitive credential leakage in logs.
- **Android Network Security Configuration & TLS**: [https://developer.android.com/privacy-and-security/security-config](https://developer.android.com/privacy-and-security/security-config)  
  *Application in Project:* Android security architecture for enforcing cleartext traffic restrictions, platform trust anchors, and TLS configuration.

---

## 6. Persistence & Caching Strategies

- **Building Offline-First Mobile Apps**: [https://developer.android.com/topic/architecture/data-layer/offline-first](https://developer.android.com/topic/architecture/data-layer/offline-first)  
  *Application in Project:* Informs the cache-first with network fallback pattern in `:core-cache`, delivering instant UI rendering (0ms cold start) while fresh network data synchronizes in the background.
- **Cache-Aside Pattern (Microsoft Azure)**: [https://learn.microsoft.com/en-us/azure/architecture/patterns/cache-aside](https://learn.microsoft.com/en-us/azure/architecture/patterns/cache-aside)  
  *Application in Project:* Guides the read-through/write-through cache lifecycle: checking local store first, fetching on miss, and updating cache entries with TTL timestamps.
- **Cash App SQLDelight (Multiplatform SQLite)**: [https://cashapp.github.io/sqldelight/](https://cashapp.github.io/sqldelight/)  
  *Application in Project:* Architectural reference for persistent local storage in KMP, generating type-safe Kotlin APIs directly from SQL schema statements on Android (SQLite/Boyer) and iOS (Native SQLite driver).
- **AndroidX Room Multiplatform**: [https://developer.android.com/kotlin/multiplatform/room](https://developer.android.com/kotlin/multiplatform/room)  
  *Application in Project:* Alternative official KMP SQLite persistence framework evaluated for future local database synchronization.

---

## 7. Observability, Telemetry & APM

- **Google SRE Book: Monitoring Distributed Systems**: [https://sre.google/sre-book/monitoring-distributed-systems/](https://sre.google/sre-book/monitoring-distributed-systems/)  
  *Application in Project:* Informs mobile Service Level Indicators (SLIs): tracking request latency (p50, p95, p99), error rate percentage, and availability through `:core-apm`.
- **Firebase Performance Monitoring**: [https://firebase.google.com/docs/perf-mon](https://firebase.google.com/docs/perf-mon)  
  *Application in Project:* Host platform observer on Android consuming latency durations recorded by the shared `TraceTimer`.
- **Apple MetricKit Framework**: [https://developer.apple.com/documentation/metrickit](https://developer.apple.com/documentation/metrickit)  
  *Application in Project:* Native iOS telemetry framework collecting power, thermal, launch, and network execution metrics generated by client apps.
- **OpenTelemetry Architecture & Tracing**: [https://opentelemetry.io/docs/concepts/what-is-opentelemetry/](https://opentelemetry.io/docs/concepts/what-is-opentelemetry/)  
  *Application in Project:* Guides the vendor-agnostic design of `:core-apm`, decoupling trace span capture from third-party vendor SDKs to prevent shared library binary bloat.

---

## 8. Client Frontend Platforms & Interoperability

### 8.1 Android Native (Jetpack Compose & AndroidX)
- **Guide to App Architecture**: [https://developer.android.com/topic/architecture](https://developer.android.com/topic/architecture)  
  *Application in Project:* Android consumer architecture documented in [`TASK.md`](file:///Users/dinakarmaurya/Documents/Personal/github-core-kmp/TASK.md) and [`docs/samples/2-guide-share-logic-native-ui-android.md`](file:///Users/dinakarmaurya/Documents/Personal/github-core-kmp/docs/samples/2-guide-share-logic-native-ui-android.md).
- **Jetpack Compose State Hoisting**: [https://developer.android.com/develop/ui/compose/state](https://developer.android.com/develop/ui/compose/state)  
  *Application in Project:* Compose screens consume pure domain models (`Repository`, `SearchResult`) from the KMP engine and hoist UI state into AndroidX ViewModels.
- **Hilt Dependency Injection on Android**: [https://developer.android.com/training/dependency-injection/hilt-android](https://developer.android.com/training/dependency-injection/hilt-android)  
  *Application in Project:* Injecting KMP Use Cases (`SearchRepositoriesUseCase`) directly into `@HiltViewModel` constructors.
- **Reference Native Android App**: [`github-cruise-android`](https://github.com/dinkar1708/github-cruise-android)  
  *Application in Project:* Complete production reference app demonstrating Hilt + Compose + Kotlin StateFlow consumption of this SDK.

### 8.2 iOS Native (SwiftUI & Swift Modern Concurrency)
- **Apple Human Interface Guidelines (HIG)**: [https://developer.apple.com/design/human-interface-guidelines](https://developer.apple.com/design/human-interface-guidelines)  
  *Application in Project:* Ensures iOS client applications maintain 100% native Apple typography, navigation stacks, and tactile feel.
- **Swift Modern Concurrency (`async/await`, `@MainActor`)**: [https://docs.swift.org/swift-book/documentation/the-swift-programming-language/concurrency/](https://docs.swift.org/swift-book/documentation/the-swift-programming-language/concurrency/)  
  *Application in Project:* Consumes suspending KMP Use Case methods annotated with `@Throws(Exception::class)` directly via native Swift `try await` without third-party callback wrappers.
- **Touchlab SKIE (Swift-Kotlin Interface Enhancer)**: [https://skie.touchlab.co/](https://skie.touchlab.co/)  
  *Application in Project:* Tooling reference for exporting Kotlin Coroutines, Flows, and Sealed Classes into idiomatic Swift enums and `AsyncSequence`.
- **Reference Native iOS App**: [`github-repo-search-ios`](https://github.com/dinkar1708/github-repo-search-ios)  
  *Application in Project:* Production SwiftUI client demonstrating `@MainActor ObservableObject` consumption of `GithubCoreKMP.xcframework`.

### 8.3 Flutter / Cross-Platform Bridge
- **Flutter Platform Channels (`MethodChannel`)**: [https://docs.flutter.dev/platform-integration/platform-channels](https://docs.flutter.dev/platform-integration/platform-channels)  
  *Application in Project:* Exposes KMP Use Case execution to Flutter / Dart via asynchronous message passing.
- **Dart FFI (Foreign Function Interface)**: [https://dart.dev/guides/libraries/c-interop](https://dart.dev/guides/libraries/c-interop)  
  *Application in Project:* High-performance direct C-ABI bridge invoking compiled KMP native binary methods directly from Dart event loops.
- **Riverpod Reactive State Management**: [https://riverpod.dev/](https://riverpod.dev/)  
  *Application in Project:* Flutter client presentation pattern using `AsyncNotifier` to consume KMP repository data.
- **Reference Flutter App**: [`flutter_riverpod_template`](https://github.com/dinkar1708/flutter_riverpod_template)  
  *Application in Project:* Production Flutter consumer application reference.

---

## 9. GitHub REST API Standards & Quota Specifications

- **GitHub REST API Documentation**: [https://docs.github.com/en/rest](https://docs.github.com/en/rest)  
  *Application in Project:* Authoritative specification for endpoint routing, HTTP verb semantics, query parameter formatting, and error payload structures.
- **GitHub Search Repositories API Endpoint**: [https://docs.github.com/en/rest/search/search#search-repositories](https://docs.github.com/en/rest/search/search#search-repositories)  
  *Application in Project:* Specification for `GET /search/repositories` query syntax (`q`), sorting (`sort=stars|forks|updated`), ordering (`order=asc|desc`), and pagination (`page`, `per_page`).
- **GitHub API Rate Limiting Specifications**: [https://docs.github.com/en/rest/using-the-rest-api/rate-limits-for-the-rest-api](https://docs.github.com/en/rest/using-the-rest-api/rate-limits-for-the-rest-api)  
  *Application in Project:* Informs [`RateLimitTracker.kt`](file:///Users/dinakarmaurya/Documents/Personal/github-core-kmp/core-network/src/commonMain/kotlin/com/github/core/network/resilience/RateLimitTracker.kt) parsing of headers:
  - `x-ratelimit-limit`: Quota maximum.
  - `x-ratelimit-remaining`: Quota remaining in active window.
  - `x-ratelimit-reset`: UTC epoch seconds of quota replenishment.
  - `x-ratelimit-used`: Requests consumed.
  - `x-ratelimit-resource`: Resource category (`search` with 30 req/min vs `core` with 5,000 req/hr).
- **GitHub Conditional Requests & ETags**: [https://docs.github.com/en/rest/overview/resources-in-the-rest-api#conditional-requests](https://docs.github.com/en/rest/overview/resources-in-the-rest-api#conditional-requests)  
  *Application in Project:* Targeted caching enhancement using `If-None-Match` and HTTP 304 Not Modified to save API rate limits.

---

## 10. Real-World Enterprise KMP Production Case Studies

The headless architectural patterns and design principles implemented in `github-core-kmp` mirror published production engineering architectures from world-class technology organizations:

### 10.1 Enterprise Production Kotlin Multiplatform (Netflix, Cash App, Forbes)
- **JetBrains Kotlin Multiplatform Enterprise Case Studies**: [https://kotlinlang.org/case-studies/](https://kotlinlang.org/case-studies/)  
  *Application in Project:* Real-world production case studies validating the **"Share logic, keep UI native"** paradigm (Netflix, Cash App, Forbes, McDonald's)—centralizing domain and networking logic in shared Kotlin while maintaining 100% native platform UI on Android and iOS.

### 10.2 Block / Cash App: Mobile Multiplatform Architecture
- **Cash App: Native UI and Multiplatform Redwood**: [https://code.cash.app/native-ui-and-multiplatform-compose-with-redwood](https://code.cash.app/native-ui-and-multiplatform-compose-with-redwood)  
  *Application in Project:* Real-world benchmark proving shared Kotlin business logic driving diverse native UI presentation engines.
- **Cash App Code Engineering Blog**: [https://code.cash.app/](https://code.cash.app/)  
  *Application in Project:* Authoritative engineering reference for multiplatform testing, SQLDelight, and Coroutine architectures.

### 10.3 Slack: Mobile Modularization & Modernization
- **Scaling Slack's Mobile Codebases**: [https://slack.engineering/stabilize-modularize-modernize-scaling-slacks-mobile-codebases-2/](https://slack.engineering/stabilize-modularize-modernize-scaling-slacks-mobile-codebases-2/)  
  *Application in Project:* Demonstrates how multi-module separation and strict dependency boundaries enable parallel development, isolate build caching, and prevent monolithic merge conflicts.

### 10.4 Touchlab: Enterprise KMP Architecture
- **Touchlab Engineering Guides**: [https://touchlab.co/](https://touchlab.co/)  
  *Application in Project:* Industry-standard guidance on Swift/Kotlin memory management, XCFramework distribution, and multiplatform crash diagnostics.

---

## 11. Testing, Verification & Continuous Delivery

- **Kotlinx Coroutines Test**: [https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-test/](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-test/)  
  *Application in Project:* Employs `runTest` and virtual time advancement to test asynchronous coroutines and delay policies deterministically without real clock delays.
- **Kotlin Test Framework**: [https://kotlinlang.org/api/latest/kotlin.test/](https://kotlinlang.org/api/latest/kotlin.test/)  
  *Application in Project:* Multiplatform test assertions (`assertEquals`, `assertTrue`, `assertNull`, `assertFailsWith`) executed across JVM, Android Host, and Apple iOS Simulator test runners.
- **Gradle Build Tool: Structuring Large Projects**: [https://docs.gradle.org/current/userguide/structuring_software_products.html](https://docs.gradle.org/current/userguide/structuring_software_products.html)  
  *Application in Project:* Manages modular project graphs, task parallelization, and build cache isolation.
- **Gradle Version Catalogs (`libs.versions.toml`)**: [https://docs.gradle.org/current/userguide/platforms.html](https://docs.gradle.org/current/userguide/platforms.html)  
  *Application in Project:* Single source of truth for all dependencies, plugin IDs, and SDK versions across all subprojects.
- **GitHub Actions for Kotlin Multiplatform**: [https://docs.github.com/en/actions](https://docs.github.com/en/actions)  
  *Application in Project:* CI/CD automation running `./gradlew allTests` on macOS runners to compile and verify JVM, Android, and Apple iOS Simulator binaries.

---

## 12. Architectural Traceability Matrix

How this project maps official platform recommendations and industry standards to concrete implementations:

| Official Recommendation | Official Doc Source | Project Implementation | Key Module & Class |
| :--- | :--- | :--- | :--- |
| **Share Logic, Keep UI Native** | [JetBrains KMP Architecture](https://kotlinlang.org/multiplatform/#choose-share-what-logic-native-ui) | Headless SDK stopping at Use Cases; zero UI or ViewModel in shared code | [`:core-domain`](file:///Users/dinakarmaurya/Documents/Personal/github-core-kmp/core-domain), [`:github-core`](file:///Users/dinakarmaurya/Documents/Personal/github-core-kmp/github-core) |
| **Clean Architecture & Inversion** | [Uncle Bob Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html) | Domain has zero external dependencies; Network and Cache implement domain interfaces | [`GithubRepository`](file:///Users/dinakarmaurya/Documents/Personal/github-core-kmp/core-domain/src/commonMain/kotlin/com/github/core/domain/repository/GithubRepository.kt), [`GithubApiService`](file:///Users/dinakarmaurya/Documents/Personal/github-core-kmp/core-network/src/commonMain/kotlin/com/github/core/network/api/GithubApiService.kt) |
| **Input Validation Guardrails** | [Kotlin Domain Validation](https://kotlinlang.org/docs/coding-conventions.html) | Fail-fast query trimming, bounds checking, and pagination constraints returning typed errors | [`QueryValidator`](file:///Users/dinakarmaurya/Documents/Personal/github-core-kmp/core-domain/src/commonMain/kotlin/com/github/core/domain/validation/QueryValidator.kt), [`PaginationValidator`](file:///Users/dinakarmaurya/Documents/Personal/github-core-kmp/core-domain/src/commonMain/kotlin/com/github/core/domain/validation/PaginationValidator.kt) |
| **Fault-Tolerant Circuit Breaker** | [Martin Fowler Circuit Breaker](https://martinfowler.com/bliki/CircuitBreaker.html) | 3-state machine (`CLOSED`, `OPEN`, `HALF_OPEN`) preventing cascading backend failure | [`CircuitBreaker.kt`](file:///Users/dinakarmaurya/Documents/Personal/github-core-kmp/core-network/src/commonMain/kotlin/com/github/core/network/resilience/CircuitBreaker.kt) |
| **Exponential Backoff Retries** | [AWS Architecture Retries](https://aws.amazon.com/blogs/architecture/exponential-backoff-and-jitter/) | Exponential delay backoff for transient 5xx server errors, failing fast on 4xx client errors | [`RetryPolicy.kt`](file:///Users/dinakarmaurya/Documents/Personal/github-core-kmp/core-network/src/commonMain/kotlin/com/github/core/network/resilience/RetryPolicy.kt) |
| **Rate Limit Quota Tracking** | [GitHub REST API Rate Limits](https://docs.github.com/en/rest/using-the-rest-api/rate-limits-for-the-rest-api) | Header-driven quota tracking (`x-ratelimit-*`) fast-failing exhausted budgets locally | [`RateLimitTracker.kt`](file:///Users/dinakarmaurya/Documents/Personal/github-core-kmp/core-network/src/commonMain/kotlin/com/github/core/network/resilience/RateLimitTracker.kt) |
| **Native Platform HTTP Engines** | [Ktor Multiplatform Engines](https://ktor.io/docs/client-engines.html) | `OkHttp` on Android/JVM for pooling; `Darwin` (`NSURLSession`) on iOS for power efficiency | [`PlatformHttpClient.kt`](file:///Users/dinakarmaurya/Documents/Personal/github-core-kmp/core-network/src/commonMain/kotlin/com/github/core/network/client/PlatformHttpClient.kt) |
| **Reflectionless Serialization** | [Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization) | Compile-time JSON encoding/decoding without reflection overhead | [`GithubDtos.kt`](file:///Users/dinakarmaurya/Documents/Personal/github-core-kmp/core-network/src/commonMain/kotlin/com/github/core/network/dto/GithubDtos.kt) |
| **Secure TLS & Apple Trust** | [Apple ATS & Trust Standards](https://developer.apple.com/documentation/security/preventing_insecure_network_connections) | Native X.509 certificate chain validation against Apple system root CAs | [`PlatformHttpClient.ios.kt`](file:///Users/dinakarmaurya/Documents/Personal/github-core-kmp/core-network/src/iosMain/kotlin/com/github/core/network/client/PlatformHttpClient.ios.kt) |
| **Offline-First & Caching** | [Google Offline-First Architecture](https://developer.android.com/topic/architecture/data-layer/offline-first) | Thread-safe in-memory cache with TTL expiration and cold-start acceleration | [`GithubCache.kt`](file:///Users/dinakarmaurya/Documents/Personal/github-core-kmp/core-cache/src/commonMain/kotlin/com/github/core/cache/GithubCache.kt) |
| **High-Precision APM Telemetry** | [Google SRE Observability](https://sre.google/sre-book/monitoring-distributed-systems/) | Monotonic execution time measurement (`TraceTimer`) decoupled from vendor SDKs | [`TraceTimer.kt`](file:///Users/dinakarmaurya/Documents/Personal/github-core-kmp/core-apm/src/commonMain/kotlin/com/github/core/apm/TraceTimer.kt) |
| **Public SDK Facade Entrypoint** | [Clean Facade Pattern](https://kotlinlang.org/docs/multiplatform.html) | Unified public entrypoint aggregating modules and producing XCFramework & AAR | [`GithubCoreSdk.kt`](file:///Users/dinakarmaurya/Documents/Personal/github-core-kmp/github-core/src/commonMain/kotlin/com/github/core/GithubCoreSdk.kt) |
| **Deterministic Multiplatform Tests** | [Kotlinx Coroutines Test](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-test/) | Virtual time execution with `runTest` and Ktor `MockEngine` for fast offline unit testing | [`NetworkTest.kt`](file:///Users/dinakarmaurya/Documents/Personal/github-core-kmp/core-network/src/commonTest/kotlin/com/github/core/network/NetworkTest.kt), [`SearchRepositoriesUseCaseTest.kt`](file:///Users/dinakarmaurya/Documents/Personal/github-core-kmp/core-domain/src/commonTest/kotlin/com/github/core/domain/SearchRepositoriesUseCaseTest.kt) |
| **Swift Concurrency Interop** | [Swift Concurrency](https://docs.swift.org/swift-book/documentation/the-swift-programming-language/concurrency/) | `@Throws(Exception::class)` suspend functions bridging seamlessly to Swift `try await` | [`SearchRepositoriesUseCase.kt`](file:///Users/dinakarmaurya/Documents/Personal/github-core-kmp/core-domain/src/commonMain/kotlin/com/github/core/domain/usecase/SearchRepositoriesUseCase.kt#L27) |

# 📚 GitHub Core KMP Documentation Portal

> 📖 **Official Standards & References:**  
> • [JetBrains: Kotlin Multiplatform Official Documentation](https://kotlinlang.org/docs/multiplatform.html)  
> • [Uncle Bob: The Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)  
> • [Master Technical References Catalog (`docs/references.md`)](./references.md)

Welcome to the technical documentation portal for **`github-core-kmp`**, an enterprise-grade Headless Kotlin Multiplatform (KMP) SDK engine.

---

## 🧭 Documentation Index

### 🌟 Executive Review & Onboarding
- [**00_START_HERE.md (Reviewer Guide)**](./00_START_HERE.md): Executive summary, 2-minute architectural walk-through, and reviewer checklist.

---

### 🏛️ Architecture Decision Records (ADRs)
Documenting significant technical trade-offs according to Michael Nygard's standard in [**`docs/adr/`**](./adr/README.md):
- [**ADR 001: Headless Boundary vs. Shared ViewModels**](./adr/001_headless_boundary_vs_shared_viewmodel.md)
- [**ADR 002: Dual Platform Ktor Engines (`Darwin` & `OkHttp`)**](./adr/002_dual_engine_ktor_darwin_okhttp.md)
- [**ADR 003: In-Engine Circuit Breaker & RateLimitTracker**](./adr/003_resilience_circuit_breaker.md)
- [**ADR 004: Thread-Safe In-Memory TTL & LRU Caching**](./adr/004_in_memory_ttl_cache_eviction.md)
- [**ADR 005: Monotonic TraceTimer vs. Third-Party APM SDKs**](./adr/005_vendor_neutral_apm_spans.md)

---

### 🏗️ Deep-Dive Architecture Specifications
In-depth technical guides located in [**`docs/architecture/`**](./architecture/README.md):
- [**01. Clean Architecture & Module Boundary**](./architecture/01_clean_architecture_and_boundary.md): Inward dependency rules, domain isolation, and facade transitive exports.
- [**02. Resilience & Circuit Breaker State Machine**](./architecture/02_resilience_and_circuit_breaker.md): 3-state state machine, proactive rate-limit header parsing, and backoff classification.
- [**03. Concurrency & Memory Safety**](./architecture/03_concurrency_and_memory_safety.md): Mutex synchronization, structured concurrency, and iOS ARC retain-cycle elimination.

---

### ⏱️ Performance Benchmarks & Telemetry
Microbenchmarks, memory allocations, and artifact size analyses in [**`docs/benchmarks/`**](./benchmarks/README.md):
- [**01. SDK Performance & Footprint Analysis**](./benchmarks/01_sdk_performance_and_footprint.md): In-memory cache speed (<1ms), API roundtrips, factory startup (<4ms), and AAR/XCFramework sizes.

---

### 📱 Client Integration Blueprints
Practical consumption guides for heterogeneous mobile frontends in [**`docs/integration/`**](./integration/README.md):
- [**01. Android Jetpack Compose Integration**](./integration/01_android_jetpack_compose_guide.md): Gradle dependency, Hilt injection, AndroidX `ViewModel`, and Compose UI binding.
- [**02. Apple iOS SwiftUI Integration**](./integration/02_ios_swiftui_spm_guide.md): SPM package linking, Swift Concurrency (`async/await`), `@Observable`, and SwiftUI.
- [**03. Flutter Riverpod Integration**](./integration/03_flutter_riverpod_bridge_guide.md): Platform MethodChannel / FFI bridging and Riverpod `AsyncNotifier`.

---

### 🧪 JetBrains Official Code-Sharing Paradigms
Adoption blueprints demonstrating the 3 official JetBrains tiers in [**`docs/samples/`**](./samples/README.md):
- [**Tier 1: Share a piece of logic**](./samples/1-guide-share-piece-of-logic.md): Consuming pure `:core-domain` input validation with zero third-party dependencies.
- [**Tier 2: Share logic, keep UI native**](./samples/2-guide-share-logic-native-ui-android.md): Full headless core engine driving native Android Compose and [**iOS SwiftUI**](./samples/2-guide-share-logic-native-ui-ios.md).
- [**Tier 3: Share both logic and UI**](./samples/3-guide-share-both-logic-and-ui.md): Compose Multiplatform sharing both logic and presentation UI.

---

### 📦 Core SDK Module Documentation
Comprehensive module specifications and responsibility catalog in [**`docs/modules/`**](./modules/README.md):
- [**Catalog Overview & Dependency Graph**](./modules/README.md): Multi-module Clean Architecture diagram and test commands.
- [**:core-domain Module Spec**](./modules/01_core_domain.md): Pure domain entities, validations, Use Cases, error taxonomy ([Local README](../core-domain/README.md)).
- [**:core-network Module Spec**](./modules/02_core_network.md): Ktor 3.x client, resilience stack, Circuit Breaker, DTO mappers ([Local README](../core-network/README.md)).
- [**:core-cache Module Spec**](./modules/03_core_cache.md): Thread-safe in-memory Mutex store, TTL freshness, LRU capacity bounds ([Local README](../core-cache/README.md)).
- [**:core-apm Module Spec**](./modules/04_core_apm.md): Monotonic `TraceTimer`, microsecond span profiling ([Local README](../core-apm/README.md)).
- [**:github-core Module Spec**](./modules/05_github_core.md): Public SDK entry point, `GithubCoreSdk.create()`, AAR & XCFramework distribution ([Local README](../github-core/README.md)).


---

### 📖 Standards & Governance
- [**Technical References (`docs/references.md`)**](./references.md): Comprehensive catalog of official platform standards, JetBrains KMP guidelines, RFCs, and security specifications.
- [**Contributing Guidelines (`docs/CONTRIBUTING.md`)**](./CONTRIBUTING.md): Git workflow, branching strategies (`dev` ➔ `stg` ➔ `main`), and Conventional Commits standards.

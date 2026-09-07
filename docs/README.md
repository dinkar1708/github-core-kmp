# GitHub Core KMP Documentation Portal

Welcome to the technical documentation for **`github-core-kmp`**, an enterprise-grade Headless Kotlin Multiplatform (KMP) SDK engine.

---

## Documentation Index

### Core SDK Modules & Architecture Deep Dives

| Module | Description | Detailed Documentation | Overview |
| :--- | :--- | :--- | :--- |
| **`:github-core` (Umbrella SDK)** | Public facade and distribution entrypoint (`GithubCoreSdk.create()`), packaging for Android AAR, iOS XCFramework, and Flutter. | [Detailed README](../github-core/README.md) | [Overview](./github-core/README.md) |
| **`:core-domain` (Core Business Logic)** | Pure Kotlin business models (`Repository`, `User`, `SearchResult`), validation rules (`QueryValidator`), error taxonomy, and Use Cases. | [Detailed README](../core-domain/README.md) | [Overview](./core-domain/README.md) |
| **`:core-network` (Ktor Networking)** | Ktor 3.x multiplatform client (OkHttp / Darwin), DTO mappers, and resilience stack (Circuit Breaker, Retries, Rate Limit Tracker). | [Detailed README](../core-network/README.md) | [Overview](./core-network/README.md) |
| **`:core-cache` (Local Persistence)** | In-memory and offline caching engine, TTL freshness policies, and cold-start acceleration. | [Detailed README](../core-cache/README.md) | [Overview](./core-cache/README.md) |
| **`:core-apm` (Telemetry & APM)** | High-precision `TraceTimer`, metric dispatching, and vendor-agnostic APM integration (Firebase, Datadog, MetricKit). | [Detailed README](../core-apm/README.md) | [Overview](./core-apm/README.md) |

### Client Adoption Guides (JetBrains Official Paradigms)

Detailed consumer guides demonstrating how external frontends adopt the SDK are organized in [**`docs/samples/`**](./samples/README.md).

| Official JetBrains Paradigm | What is Shared? | Integration Guide | Executable Sample Project |
| :--- | :--- | :--- | :--- |
| [**1. Share a piece of logic**](https://kotlinlang.org/multiplatform/#choose-share-what-piece-of-logic) | Validation rules & domain models only (Zero third-party deps) | [1. Read Guide](./samples/1-guide-share-piece-of-logic.md) | [`sample/sample-share-piece-of-logic`](../sample/sample-share-piece-of-logic) |
| [**2. Share logic but keep UI native**](https://kotlinlang.org/multiplatform/#choose-share-what-logic-native-ui) | Full Headless SDK engine (Network, Cache, Use Cases) + 100% Native UI | • [2. Android Guide](./samples/2-guide-share-logic-native-ui-android.md)<br>• [2. iOS Guide](./samples/2-guide-share-logic-native-ui-ios.md) | • [`sample/sample-share-logic-native-ui-android`](../sample/sample-share-logic-native-ui-android)<br>• [`sample/sample-share-logic-native-ui-ios`](../sample/sample-share-logic-native-ui-ios) |
| [**3. Share both logic and UI**](https://kotlinlang.org/multiplatform/#choose-share-what-both-logic-ui) | Shared SDK logic + Shared Compose Multiplatform UI across platforms | [3. Read Guide](./samples/3-guide-share-both-logic-and-ui.md) | [`sample/sample-share-both-logic-and-ui`](../sample/sample-share-both-logic-and-ui) |

---

### Architecture & Component Flow

> [!NOTE]
> This architecture implements JetBrains' official Kotlin Multiplatform architectural tier: [**"One logic layer, native experience" (`logic-native-ui`)**](https://kotlinlang.org/multiplatform/#choose-share-what-logic-native-ui). The shared engine handles all data models, validations, networking, and caching, while native client frontends maintain complete autonomy over UI and presentation state.

```text
┌────────────────────────────────────────────────────────────────────────┐
│                   github-core-kmp (Pure SDK Engine)                    │
│                                                                        │
│   ┌──────────────┐     ┌──────────────┐     ┌──────────────┐          │
│   │ :core-domain │ ◄── │:core-network │ ◄── │ :core-cache  │          │
│   │ (Use Cases)  │     │(Ktor Client) │     │ (Storage)    │          │
│   └──────▲───────┘     └──────────────┘     └──────────────┘          │
│          │                    │                    │                  │
│          └────────────────────┼────────────────────┘                  │
│                               │                                        │
│                        ┌──────▼───────┐                               │
│                        │  :core-apm   │ (TraceTimer / APM)            │
│                        └──────▲───────┘                               │
│                               │                                        │
│                     ┌─────────┴──────────┐                            │
│                     │    :github-core    │ (Umbrella SDK Facade)      │
│                     └─────────┬──────────┘                            │
└───────────────────────────────┼────────────────────────────────────────┘
                                │
                                ▼
         Strict Headless Boundary (Zero UI or ViewModels in Core)
                                │
      ┌─────────────────────────┼─────────────────────────┐
      ▼                         ▼                         ▼
   1. Piece of Logic        2. Logic + Native UI      3. Logic + Shared UI
  (sample-share-piece-      (sample-share-logic-      (sample-share-both-logic-
   of-logic)                 native-ui-android & ios)  and-ui)
  Imports ONLY :core-domain  • Android (Compose)       Compose Multiplatform
  for query validation       • iOS (SwiftUI)           across Android/iOS/Desktop
```

---

## Quick Test Commands

```bash
# Run all tests across all KMP modules
./gradlew check

# Build the Android sample application
cd sample/sample-share-logic-native-ui-android && ./gradlew assembleDebug

# Build the iOS sample application
cd sample/sample-share-logic-native-ui-ios && xcodebuild -project sample-iOS.xcodeproj -scheme sample-iOS -destination 'generic/platform=iOS Simulator' build
```

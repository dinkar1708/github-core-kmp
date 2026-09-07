# 📚 GitHub Core KMP Documentation Portal

Welcome to the technical documentation for **`github-core-kmp`**, an enterprise-grade Headless Kotlin Multiplatform (KMP) SDK engine.

---

## 🧭 Documentation Index

| Guide / Module | Description | Detailed Documentation | Overview |
| :--- | :--- | :--- | :--- |
| **🚀 KMP Android Usage Guide** | Complete guide on how the sample Android app integrates the SDK, composite builds, permissions, Use Case execution, and Jetpack Compose UI state binding. | [Read Guide](./kmp-usage-guide-android.md) | [Guide](./kmp-usage-guide-android.md) |
| **🍏 KMP iOS Usage Guide** | Integration guide for iOS and SwiftUI using the static framework, Swift async/await interop, and clean rebuild lifecycle. | [Read Guide](./kmp-usage-guide-ios.md) | [Guide](./kmp-usage-guide-ios.md) |
| **📦 `:github-core` (Umbrella SDK)** | Public facade and distribution entrypoint (`GithubCoreSdk.create()`), packaging for Android AAR, iOS XCFramework, and Flutter. | [Detailed README](../github-core/README.md) | [Overview](./github-core/README.md) |
| **🎯 `:core-domain` (Core Business Logic)** | Pure Kotlin business models (`Repository`, `User`, `SearchResult`), validation rules (`QueryValidator`), error taxonomy, and Use Cases. | [Detailed README](../core-domain/README.md) | [Overview](./core-domain/README.md) |
| **🌐 `:core-network` (Ktor Networking)** | Ktor 3.x multiplatform client (OkHttp / Darwin), DTO mappers, and resilience stack (Circuit Breaker, Retries, Rate Limit Tracker). | [Detailed README](../core-network/README.md) | [Overview](./core-network/README.md) |
| **💾 `:core-cache` (Local Persistence)** | In-memory and offline caching engine, TTL freshness policies, and cold-start acceleration. | [Detailed README](../core-cache/README.md) | [Overview](./core-cache/README.md) |
| **📊 `:core-apm` (Telemetry & APM)** | High-precision `TraceTimer`, metric dispatching, and vendor-agnostic APM integration (Firebase, Datadog, MetricKit). | [Detailed README](../core-apm/README.md) | [Overview](./core-apm/README.md) |

---

## 🏛️ Architecture & Component Flow

> [!NOTE]
> This architecture implements JetBrains' official Kotlin Multiplatform architectural tier: [**"One logic layer, native experience" (`logic-native-ui`)**](https://kotlinlang.org/multiplatform/#choose-share-what-logic-native-ui). The shared engine handles all data models, validations, networking, and caching, while native client frontends maintain complete autonomy over UI and presentation state.

```text
┌────────────────────────────────────────────────────────────────────────┐
│                        github-core-kmp Engine                          │
│                                                                        │
│   ┌──────────────┐     ┌──────────────┐     ┌──────────────┐          │
│   │ :core-domain │ ◄── │:core-network │ ◄── │ :core-cache  │          │
│   │ (Use Cases)  │     │(Ktor Client) │     │ (Storage)    │          │
│   └──────▲───────┘     └──────────────┘     └──────────────┘          │
│          │                    │                    │                  │
│          └────────────────────┼────────────────────┘                  │
│                               │                                        │
│                        ┌──────▼───────┐                               │
│                        │  :core-apm   │ (TraceTimer / Latency)        │
│                        └──────▲───────┘                               │
│                               │                                        │
│                     ┌─────────┴──────────┐                            │
│                     │    :github-core    │ (Umbrella SDK Facade)      │
│                     └─────────┬──────────┘                            │
└───────────────────────────────┼────────────────────────────────────────┘
                                │
                                ▼
       ┌─────────────────────────────────────────────────┐
       │            Strict Headless Boundary             │
       │   (No shared ViewModels or Presentation State)  │
       └────────────────────────┬────────────────────────┘
                                │
        ┌───────────────────────┼───────────────────────┐
        ▼                       ▼                       ▼
 🤖 Android (Kotlin)       🍏 iOS (Swift)         📱 Flutter (Dart)
 Jetpack Compose UI        SwiftUI UI             Flutter UI
 Sample App                Sample App             Riverpod
 (`sample/sample-android`) (`sample/sample-iOS`)
```

---

## 🧪 Quick Test Commands

```bash
# Run all tests across all KMP modules
./gradlew check

# Build the Android sample application
cd sample/sample-android && ./gradlew assembleDebug
```

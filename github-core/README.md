# 🚀 `:github-core` (Umbrella SDK Module)

The **public facade and framework distribution module** for the GitHub Core KMP SDK.

---

## 🎯 Architectural Responsibility & Boundary

* **Unified SDK Entrypoint:** Aggregates `:core-domain`, `:core-network`, `:core-cache`, and `:core-apm`.
* **Framework Packaging (iOS / SPM):** Produces `GithubCoreKMP.xcframework` / static framework exporting all public domain models, Use Cases, and APM configurations.
* **Android Library Packaging (AAR / Maven):** Configured for Android AAR distribution via MavenLocal or MavenCentral.
* **SDK Dependency Injection / Factory:** Provides high-level SDK factory (`GithubCoreSdk.create()`) to easily initialize network, caching, and use cases on any platform.

---

## 🏗️ Structure

```text
github-core/
├── src/
│   ├── commonMain/kotlin/com/github/core/
│   │   ├── GithubCoreSdk.kt      # High-level SDK initializer & factory
│   │   └── di/                   # Common dependency wiring
│   └── commonTest/               # Full end-to-end SDK smoke tests
└── build.gradle.kts
```

---

## 🧪 Running Tests

### 1. Standard Run (uses Gradle cache):
```bash
./gradlew :github-core:allTests
```

### 2. Force Re-run & Print Live Logs (bypasses cache):
```bash
./gradlew :github-core:allTests --rerun-tasks
```

### 3. Target-Specific Test Commands:
```bash
# Android Host / JVM SDK tests
./gradlew :github-core:testAndroidHostTest --rerun-tasks

# iOS Simulator SDK tests
./gradlew :github-core:iosSimulatorArm64Test --rerun-tasks
```

---

## 📦 How Clients Consume `:github-core`

* **iOS (SwiftUI):** Consumes `GithubCoreKMP` via Swift Package Manager (SPM) or XCFramework.
* **Android (Jetpack Compose):** Imports `implementation("com.github.core:github-core:x.y.z")` via Gradle.
* **Flutter:** Bridges calls to `GithubCoreSdk` via platform channel / Dart FFI.

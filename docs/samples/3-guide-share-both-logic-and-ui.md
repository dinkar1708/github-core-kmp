# 3. Share both logic and UI — Compose Multiplatform Guide

> **Official JetBrains Documentation:** [Choose what to share: Share both logic and UI](https://kotlinlang.org/multiplatform/#choose-share-what-both-logic-ui)

A comprehensive guide to building a cross-platform application sharing both business logic and user interface using **Compose Multiplatform (CMP)**, referencing [`sample/sample-share-both-logic-and-ui`](../../sample/sample-share-both-logic-and-ui).

---

## Core Concept & Purpose

> [!NOTE]
> This architecture directly implements JetBrains' official Kotlin Multiplatform recommendation: [**"Maximum reuse, faster delivery" (`both-logic-ui`)**](https://kotlinlang.org/multiplatform/#choose-share-what-both-logic-ui) — using Kotlin with Compose Multiplatform to share up to 100% of your app code (including UI) across Android, iOS, and Desktop from a single unified codebase.

### Why & When to Choose Type 3:
* **The Goal:** Maximum code reuse and fast time-to-market.
* **What is Shared in KMP (~95%+):** Both the headless data/business engine (`:github-core`) AND the user interface. A single `@Composable` UI codebase powers Android, iOS (via Skiko rendering on a `ComposeUIViewController`), and Desktop.
* **Contrast with Type 1 & Type 2:** In Type 1 and Type 2, the iOS UI is written natively in SwiftUI. In Type 3, there is zero SwiftUI to maintain for screens — Kotlin Compose renders directly on iOS.

### Architectural Boundary:
* **Headless SDK (`github-core-kmp`):** Remains 100% pure and completely headless. It contains zero UI dependencies, zero Compose runtime libraries, and has zero knowledge of any child consumer projects.
* **Compose Multiplatform App (`sample/sample-share-both-logic-and-ui`):** A standalone multiplatform consumer application that imports the headless `:github-core` engine for networking, caching, and use cases, and implements a shared `@Composable` UI layer running on:
  - Android (Native Jetpack Compose runtime)
  - iOS (`ComposeUIViewController` rendering via Skiko canvas)
  - Desktop (JVM Skia windowing)
* **Strict SDK Purity:** The core SDK remains completely decoupled. If a consumer team prefers native SwiftUI, they use Tier 2 (`sample-share-logic-native-ui-ios`); if a team prefers 100% shared UI, they consume the same SDK via Tier 3 (`sample-share-both-logic-and-ui`).

---

## 🏛️ Packaging & Linking Type: Static vs. Dynamic

### Configuration
In `sample/sample-share-both-logic-and-ui/composeApp/build.gradle.kts`, the shared Compose UI module consumes `:github-core` and exports it into the native target binaries:

```kotlin
kotlin {
    androidTarget()
    listOf(iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            export(project(":github-core"))
        }
    }
    jvm("desktop")
}
```

### Why Static?
* **Direct Monolithic Linkage:** On iOS, Kotlin Native statically compiles both the Compose UI runtime (Skiko) and the headless `:github-core` SDK into a single `ComposeApp.framework`.
* **Zero Missing Dynamic Library Crashes:** Eliminates `dyld: Library not loaded` errors on iOS device launch.
* **Single Binary Deployment:** The iOS Xcode wrapper project only needs to link `ComposeApp.framework`, which contains both the UI and the underlying KMP data engine.

### What if Dynamic?
* Using a dynamic framework for Compose Multiplatform on iOS increases application startup time due to dynamic symbol resolution and requires configuring "Embed & Sign" in Xcode. Static linking is JetBrains' recommended standard for CMP.

---

## 📦 Build Setup & Sample Location

### Proposed Sample Folder Location:
```text
github-core-kmp/
├── core-domain/
├── core-network/
├── core-cache/
├── core-apm/
├── github-core/                                # Pure Headless SDK (unaware of any UI or sample)
└── sample/
    ├── sample-share-piece-of-logic/            # Tier 1 Sample (Consumes only :core-domain)
    ├── sample-share-logic-native-ui-android/   # Tier 2 Android Sample (Native Jetpack Compose)
    ├── sample-share-logic-native-ui-ios/       # Tier 2 iOS Sample (Native SwiftUI)
    └── sample-share-both-logic-and-ui/         # Tier 3 Sample (Shared Compose Multiplatform UI)
        ├── composeApp/                         # Shared UI module
        └── iosApp/                             # Minimal iOS Xcode host shell
```

### 1. Composite Build Setup
In `sample/sample-share-both-logic-and-ui/settings.gradle.kts`:

```kotlin
includeBuild("../../") {
    dependencySubstitution {
        substitute(module("com.github.core:github-core")).using(project(":github-core"))
    }
}
```

### 2. Dependency Declaration
In `sample/sample-share-both-logic-and-ui/composeApp/build.gradle.kts`:

```kotlin
dependencies {
    // Pure Headless SDK engine:
    commonMainImplementation("com.github.core:github-core")

    // Compose Multiplatform UI:
    commonMainImplementation(compose.runtime)
    commonMainImplementation(compose.foundation)
    commonMainImplementation(compose.material3)
    commonMainImplementation(compose.ui)
}
```

---

## 🧹 SDK Clean & Rebuilding Lifecycle (`./gradlew clean`)

### Clean Behavior
* **Android & Desktop:** Managed automatically by Gradle Composite Builds. Running `./gradlew clean` in the root repository wipes artifacts, but running `./gradlew :composeApp:assembleDebug` or `:composeApp:run` immediately recompiles both `:github-core` and the shared UI.
* **iOS:** The minimal Xcode project (`sample/sample-share-both-logic-and-ui/iosApp`) utilizes Gradle's `embedAndSignAppleFrameworkForXcode` task, meaning Xcode triggers Gradle automatically during build time.

### How to Rebuild
```bash
# Android
cd sample/sample-share-both-logic-and-ui && ./gradlew :composeApp:assembleDebug

# Desktop
cd sample/sample-share-both-logic-and-ui && ./gradlew :composeApp:run

# iOS Framework
cd sample/sample-share-both-logic-and-ui && ./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
```

---

## 🔑 Key Definitions & Interop Notes

| Component | Source / Package | Role in Compose Multiplatform |
| :--- | :--- | :--- |
| **`GithubCoreSdk`** | `com.github.core.GithubCoreSdk` | Initialized once in shared code (`commonMain`) to provide networking and cache. |
| **`SearchRepositoriesUseCase`** | `com.github.core.domain.usecase.SearchRepositoriesUseCase` | Invoked directly from shared `@Composable` coroutine scopes. |
| **`RepoSearchScreen`** | `sample.cmp.ui.RepoSearchScreen` | 100% shared Composable screen rendering identically on Android, iOS, and Desktop. |
| **`ComposeUIViewController`** | `androidx.compose.ui.window.ComposeUIViewController` | iOS platform bridge wrapping the shared Composable inside a native `UIViewController`. |

---

## 💻 Implementation in Client App

### 1. Shared Composable Screen (`commonMain`)
In `sample/sample-share-both-logic-and-ui/composeApp/src/commonMain/kotlin/sample/cmp/ui/RepoSearchScreen.kt`:

```kotlin
@Composable
fun RepoSearchScreen(searchUseCase: SearchRepositoriesUseCase) {
    var query by remember { mutableStateOf("kotlin") }
    var isLoading by remember { mutableStateOf(false) }
    var repositories by remember { mutableStateOf<List<Repository>>(emptyList()) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.weight(1f)
            )
            Button(onClick = {
                scope.launch {
                    isLoading = true
                    val result = searchUseCase.execute(query.trim())
                    result.onSuccess { repositories = it.items }
                    isLoading = false
                }
            }) {
                Text("Search")
            }
        }

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            LazyColumn {
                items(repositories, key = { it.id }) { repo ->
                    Text(repo.fullName, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
```

### 2. iOS Entry Point (`iosMain`)
In `sample/sample-share-both-logic-and-ui/composeApp/src/iosMain/kotlin/sample/cmp/MainViewController.kt`:

```kotlin
import androidx.compose.ui.window.ComposeUIViewController
import com.github.core.GithubCoreSdk
import com.github.core.domain.usecase.SearchRepositoriesUseCase
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    val sdk = GithubCoreSdk.create()
    val searchUseCase = SearchRepositoriesUseCase(sdk.networkClient.apiService)
    
    return ComposeUIViewController {
        RepoSearchScreen(searchUseCase = searchUseCase)
    }
}
```

### 3. iOS Xcode Shell (`iosApp/ContentView.swift`)
The iOS app simply presents the `ComposeUIViewController`:

```swift
import SwiftUI
import ComposeApp

struct ContentView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
```

---

## ⚡ Error Handling Patterns

Error handling is written once in `commonMain` and works universally across all platforms:

```kotlin
result.onFailure { error ->
    when (error) {
        is DomainError.ValidationError -> showSnackbar("Validation: ${error.message}")
        is DomainError.RateLimitExceededError -> showSnackbar("Rate limit reached: reset in ${error.resetTimeSeconds}s")
        is DomainError.NetworkError -> showSnackbar("Network failure (${error.statusCode})")
        else -> showSnackbar("Error: ${error.message}")
    }
}
```

---

## 🧪 Build & Run Commands

```bash
# 1. Run Android Application
cd sample/sample-share-both-logic-and-ui && ./gradlew :composeApp:assembleDebug

# 2. Run Desktop Application (JVM)
cd sample/sample-share-both-logic-and-ui && ./gradlew :composeApp:run

# 3. Build iOS Framework
cd sample/sample-share-both-logic-and-ui && ./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
```

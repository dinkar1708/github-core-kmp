# 2. Share logic but keep UI native — iOS Guide

> **Official JetBrains Documentation:** [Choose what to share: Share logic but keep UI native](https://kotlinlang.org/multiplatform/#choose-share-what-logic-native-ui)

A concise guide to integrating the headless **`github-core-kmp`** SDK into an iOS application, using [`sample/sample-share-logic-native-ui-ios`](../../sample/sample-share-logic-native-ui-ios) as the reference.

---

## Core Concept & Purpose

> [!NOTE]
> This architecture directly implements JetBrains' official Kotlin Multiplatform recommendation: [**"One logic layer, native experience" (`logic-native-ui`)**](https://kotlinlang.org/multiplatform/#choose-share-what-logic-native-ui) — writing data handling and business logic once in KMP while keeping the UI fully native for maximum platform fidelity and performance.

### Why & When to Choose Type 2 (Our Core Engine Model):
* **The Goal:** Maximum platform fidelity with a unified data & business engine.
* **What is Shared in KMP (~70%):** The entire Headless SDK (`:core-network`, `:core-cache`, `:core-apm`, `:core-domain`). KMP handles all HTTP requests (Ktor Darwin engine), SQLite database caching (SQLDelight), latency telemetry, and Use Cases.
* **What is NOT Shared in KMP:** 
  * **UI is 100% Native:** iOS engineers write standard SwiftUI views, `@StateObject` / `@Published` observable view models, and consume suspending functions seamlessly with Swift concurrency (`async`/`await`).
  * **No UI or ViewModels in Core:** The SDK stops cleanly at the Use Case boundary, respecting Apple's ARC memory management and avoiding ViewModel lifecycle leaks.

---

## 🏛️ Packaging & Linking Type: Static vs. Dynamic

### Configuration
In [`github-core/build.gradle.kts`](../../github-core/build.gradle.kts), the framework binary is configured as a **Static Framework**:

```kotlin
iosTarget.binaries.framework {
    baseName = "GithubCoreKMP"
    isStatic = true
    export(project(":core-domain"))
    export(project(":core-network"))
    export(project(":core-cache"))
    export(project(":core-apm"))
}
```

### Why Static?
* **Direct Compilation into App Binary:** The framework's object code is statically linked directly into the `sample-share-logic-native-ui-ios` host executable at link time.
* **No "Embed Frameworks" Step in Xcode:** Unlike dynamic frameworks, static frameworks **must NOT** be embedded in Xcode's *Embed Frameworks* build phase (or set to "Embed & Sign" in General settings). Linking it via `OTHER_LDFLAGS = -framework GithubCoreKMP` is sufficient.
* **Zero Missing Dynamic Library Crashes:** Eliminates `dyld: Library not loaded` / `image not found` crashes on launch.
* **Faster Startup Time:** Avoids dyld image loading and dynamic symbol rebinding overhead during cold-start.

### What if Dynamic?
* If switched to a **Dynamic Framework** (`isStatic = false`), Xcode would require adding `GithubCoreKMP.framework` to **Frameworks, Libraries, and Embedded Content** with **"Embed & Sign"**. Otherwise, the app compiles but crashes immediately upon launch on devices and simulators with a missing image error.

---

## 📦 Build Setup & Configuration

### 1. Build the Shared Framework
Compile the debug framework for the iOS simulator target:

```bash
./gradlew :github-core:linkDebugFrameworkIosSimulatorArm64
```

Output artifact:
```
github-core/build/bin/iosSimulatorArm64/debugFramework/GithubCoreKMP.framework
```
*(For physical devices or release builds, use `:github-core:linkReleaseFrameworkIosArm64` or assemble an XCFramework).*

### 2. Framework Search Paths
In [`sample-iOS.xcodeproj`](../../sample/sample-share-logic-native-ui-ios/sample-iOS.xcodeproj), add `FRAMEWORK_SEARCH_PATHS`:
```text
$(SRCROOT)/../../github-core/build/bin/iosSimulatorArm64/debugFramework
```

### 3. Other Linker Flags
Add `OTHER_LDFLAGS`:
```text
-framework GithubCoreKMP
```

### 4. Excluded Architectures
Add `EXCLUDED_ARCHS`:
```text
"EXCLUDED_ARCHS[sdk=iphonesimulator*]" = "x86_64"
```
*(Ensures Apple Silicon simulators build cleanly without architecture mismatch).*

---

## 🧹 SDK Clean & Rebuilding Lifecycle (`./gradlew clean`)

### Clean Behavior
* Unlike Android (which auto-recompiles via composite builds), Xcode looks for the pre-built framework binary located in `github-core/build/bin/...`.
* If you run `./gradlew clean` in the root repository, the `build/` directory is deleted.
* Attempting to build in Xcode immediately after a clean will fail with:
  ```text
  ld: framework not found GithubCoreKMP
  ```

### How to Rebuild
1. **Manual Command:** Re-run the Gradle framework task after any clean:
   ```bash
   ./gradlew :github-core:linkDebugFrameworkIosSimulatorArm64
   ```
2. **Automating via Xcode Run Script (Optional):** Add a Run Script phase before *Compile Sources* in Xcode target build phases:
   ```bash
   cd "$SRCROOT/../.."
   ./gradlew :github-core:linkDebugFrameworkIosSimulatorArm64
   ```
   This ensures hitting **Cmd + B** in Xcode automatically compiles the KMP framework whenever it is missing or after a clean.

---

## 🔑 Key Definitions & Interop Notes

### Swift Interoperability Details:
* **Constructor Default Arguments:** Kotlin default parameters do not map to Swift constructors. [`SearchRepositoriesUseCase`](../../core-domain/src/commonMain/kotlin/com/github/core/domain/usecase/SearchRepositoriesUseCase.kt) provides an explicit secondary constructor `constructor(repository: GithubRepository)` for clean Swift initialization.
* **Async / Await with `@Throws`:** Kotlin Native `suspend` functions require `@Throws(Exception::class)` to translate to throwing Swift `try await` functions, preventing runtime crashes.
* **Handling Inline `Result<T>`:** `kotlin.Result` is a Kotlin inline value class that gets obscured in Objective-C. Calling `search(query:)` unwraps `getOrThrow()`, passing strongly-typed `SearchResult<Repository>` directly to Swift.

### Summary Mapping Table:

| Kotlin / KMP Component | iOS Swift Consumer Symbol | Role |
| :--- | :--- | :--- |
| `GithubCoreSdk.create()` | `GithubCoreSdk.companion.create()` | Access via the companion object factory. |
| `SearchRepositoriesUseCase` | `SearchRepositoriesUseCase(repository:)` | Initialized with `sdk.networkClient.apiService`. |
| `@Throws suspend fun search()` | `try await searchUseCase.search(query:)` | Suspending throwing method bridging to Swift `async/await`. |
| `Repository` | `repo.fullName`, `repo.description_` | Renamed with trailing underscore to avoid collision with `NSObject.description`. |
| `SearchResult.items` | `result.items as? [Repository]` | Cast to native Swift typed array. |
| `DomainError` | `do { ... } catch { ... }` | Handled via Swift standard error catching. |

---

## 💻 Implementation in Client App

The iOS sample app is organized into dedicated components matching clean architecture boundaries:

```text
sample/sample-share-logic-native-ui-ios/sample-iOS/
├── ContentView.swift             # Root entry view hosting the search screen
└── Search/
    ├── RepoSearchViewModel.swift # ObservableObject handling async SDK calls
    ├── RepoSearchScreen.swift    # SwiftUI screen container (search bar, states, list)
    └── RepositoryRow.swift       # Subview rendering individual repository cards
```

### 1. Root Host View
In [`ContentView.swift`](../../sample/sample-share-logic-native-ui-ios/sample-iOS/ContentView.swift):

```swift
import SwiftUI

struct ContentView: View {
    var body: some View {
        RepoSearchScreen()
    }
}
```

### 2. State & ViewModel Management
In [`Search/RepoSearchViewModel.swift`](../../sample/sample-share-logic-native-ui-ios/sample-iOS/Search/RepoSearchViewModel.swift):

```swift
import SwiftUI
import Combine
import GithubCoreKMP

@MainActor
class RepoSearchViewModel: ObservableObject {
    @Published var query: String = "kotlin"
    @Published var isLoading: Bool = false
    @Published var errorMessage: String? = nil
    @Published var repositories: [Repository] = []

    private let searchUseCase: SearchRepositoriesUseCase

    init(searchUseCase: SearchRepositoriesUseCase? = nil) {
        if let useCase = searchUseCase {
            self.searchUseCase = useCase
        } else {
            let sdk = GithubCoreSdk.companion.create()
            self.searchUseCase = SearchRepositoriesUseCase(repository: sdk.networkClient.apiService)
        }
    }

    func search(searchQuery: String) async {
        let trimmed = searchQuery.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else { return }

        isLoading = true
        errorMessage = nil

        do {
            let result = try await searchUseCase.search(query: trimmed)
            self.repositories = result.items as? [Repository] ?? []
            self.isLoading = false
        } catch {
            self.errorMessage = error.localizedDescription
            self.isLoading = false
        }
    }
}
```

### 3. Screen Container
In [`Search/RepoSearchScreen.swift`](../../sample/sample-share-logic-native-ui-ios/sample-iOS/Search/RepoSearchScreen.swift):

```swift
struct RepoSearchScreen: View {
    @StateObject private var viewModel: RepoSearchViewModel

    init(searchUseCase: SearchRepositoriesUseCase? = nil) {
        _viewModel = StateObject(wrappedValue: RepoSearchViewModel(searchUseCase: searchUseCase))
    }

    var body: some View {
        NavigationStack {
            VStack(spacing: 16) {
                // Search bar with action button
                HStack {
                    TextField("Search repositories", text: $viewModel.query)
                        .textFieldStyle(.roundedBorder)
                    Button("Search") {
                        Task { await viewModel.search(searchQuery: viewModel.query) }
                    }
                }
                .padding(.horizontal)

                // State presentation
                if viewModel.isLoading {
                    ProgressView("Searching repositories...")
                } else if let error = viewModel.errorMessage {
                    Text(error).foregroundStyle(.red)
                } else {
                    List(viewModel.repositories, id: \.id) { repo in
                        RepositoryRow(repo: repo)
                    }
                }
            }
            .navigationTitle("GitHub Core SDK")
        }
    }
}
```

### 4. Item Row Component
In [`Search/RepositoryRow.swift`](../../sample/sample-share-logic-native-ui-ios/sample-iOS/Search/RepositoryRow.swift):

```swift
struct RepositoryRow: View {
    let repo: Repository

    var body: some View {
        VStack(alignment: .leading) {
            Text(repo.fullName).font(.headline)
            if let desc = repo.description_ {
                Text(desc).font(.subheadline).foregroundStyle(.secondary)
            }
            Text("★ \(repo.stargazersCount) | \(repo.language ?? "")").font(.caption)
        }
    }
}
```

---

## ⚡ Error Handling Patterns

Handle exceptions thrown from the shared KMP layer using standard Swift `do-catch`:

```swift
do {
    let result = try await searchUseCase.search(query: query)
    self.repositories = result.items as? [Repository] ?? []
} catch {
    // Captures ValidationError, RateLimitExceeded, NetworkError
    self.errorMessage = error.localizedDescription
}
```

---

## 🧪 Build & Run Commands

```bash
# 1. Build the shared KMP framework for iOS Simulator
./gradlew :github-core:linkDebugFrameworkIosSimulatorArm64

# 2. Build the iOS sample application
cd sample/sample-share-logic-native-ui-ios && xcodebuild -project sample-iOS.xcodeproj -scheme sample-iOS -destination 'generic/platform=iOS Simulator' build

# 3. Install & launch in a booted simulator (optional)
xcrun simctl install booted <path-to-sample-iOS.app>
xcrun simctl launch booted com.sample.ios.sample-iOS

# 4. Run all shared KMP tests across all modules
cd ../.. && ./gradlew check
```

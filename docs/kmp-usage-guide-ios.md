# 🍏 iOS KMP Usage & Sample App Guide

A concise guide to integrating the headless **`github-core-kmp`** SDK into an iOS application, using [`sample/sample-iOS`](../sample/sample-iOS) as the reference.

---

## 💡 Core Concept: Headless Boundary for iOS

The shared SDK engine provides the business logic, networking, and caching, while the iOS application retains full control over the user interface and presentation state:

* **SDK (`github-core-kmp`):** Domain models, validation rules, repository search use case, Ktor Darwin HTTP engine, and caching.
* **iOS App (`sample-iOS`):** Native SwiftUI views, `@StateObject` / `@Published` observable view models, and Swift concurrency (`async`/`await`).
* **Strict Boundary:** No UI components, ViewModels, or platform presentation state inside the shared KMP engine.

---

## 🏛️ Framework Type: Static vs. Dynamic (`isStatic = true`)

In [`github-core/build.gradle.kts`](../github-core/build.gradle.kts), the framework is configured as a **Static Framework**:

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

### Why Static Framework?
* **Direct Compilation into App Binary:** The framework's object code is statically linked directly into the `sample-iOS` executable.
* **No "Embed Frameworks" Step in Xcode:** Unlike dynamic frameworks, static frameworks **must NOT** be embedded in Xcode's *Embed Frameworks* build phase (or set to "Embed & Sign" in General settings). Linking it via `OTHER_LDFLAGS = -framework GithubCoreKMP` is sufficient.
* **Zero Missing Dynamic Library Crashes:** Eliminates `dyld: Library not loaded` / `image not found` crashes on launch.
* **Faster Startup Time:** Avoids dyld image loading and dynamic symbol rebinding overhead at application cold-start.

> [!NOTE]
> If switched to a **Dynamic Framework** (`isStatic = false`), Xcode would require adding `GithubCoreKMP.framework` to **Frameworks, Libraries, and Embedded Content** with **"Embed & Sign"**. Otherwise, the app compiles but crashes immediately upon launch on devices and simulators.

---

## 📦 Framework Generation & Xcode Setup

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

### 2. Xcode Build Settings
In [`sample-iOS.xcodeproj`](../sample/sample-iOS/sample-iOS.xcodeproj):

1. **Framework Search Paths (`FRAMEWORK_SEARCH_PATHS`):**
   ```text
   $(SRCROOT)/../../github-core/build/bin/iosSimulatorArm64/debugFramework
   ```
2. **Other Linker Flags (`OTHER_LDFLAGS`):**
   ```text
   -framework GithubCoreKMP
   ```
3. **Excluded Architectures (`EXCLUDED_ARCHS`):**
   ```text
   "EXCLUDED_ARCHS[sdk=iphonesimulator*]" = "x86_64"
   ```
   *(Ensures Apple Silicon simulators build cleanly without architecture mismatch).*

---

## 🧹 SDK Clean & Rebuilding Behavior (`./gradlew clean`)

### Understanding the Difference between Android and iOS:
* **Android (`sample-android`):** Uses Gradle Composite Builds (`includeBuild("../../")`). If you run `./gradlew clean`, Android Studio / Gradle automatically detects missing binaries and recompiles the KMP engine on the fly during `./gradlew assembleDebug`.
* **iOS (`sample-iOS`):** Xcode does not directly trigger Gradle tasks by default. It looks for the pre-built framework binary located in `github-core/build/bin/...`.
  * If you run `./gradlew clean` in the root repository, the `build/` directory is deleted.
  * Attempting to build in Xcode immediately after a clean will fail with:
    ```text
    ld: framework not found GithubCoreKMP
    ```

### How to Resolve:
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

## 🔑 Swift Interoperability & Design Notes

### 1. Constructor Default Arguments
In Kotlin, default constructor parameters (`QueryValidator = QueryValidator()`) are not exported as default parameters to Objective-C / Swift headers. To prevent Swift callers from needing to supply internal validator classes, [`SearchRepositoriesUseCase`](../core-domain/src/commonMain/kotlin/com/github/core/domain/usecase/SearchRepositoriesUseCase.kt) provides an explicit secondary constructor:
```kotlin
constructor(repository: GithubRepository) : this(repository, QueryValidator(), PaginationValidator())
```
Swift can now initialize cleanly:
```swift
SearchRepositoriesUseCase(repository: sdk.networkClient.apiService)
```

### 2. Async / Await with `@Throws`
In Kotlin Native, a `suspend` function only translates to a throwing Swift async function (`try await`) if annotated with `@Throws`:
```kotlin
@Throws(Exception::class)
suspend fun search(query: String): SearchResult<Repository> = execute(query).getOrThrow()
```
* **Without `@Throws`:** Any unhandled Kotlin coroutine exception will crash the iOS app runtime.
* **With `@Throws`:** Exceptions convert to catchable Swift `Error`s within standard `do { try await ... } catch { ... }` blocks.

### 3. Handling Kotlin `Result<T>` Inline Class
Kotlin's `kotlin.Result` is an inline value class (`value class`). In Objective-C and Swift interop, inline classes get boxed and obscured into generic `Any?`. The dedicated `search(query: String)` method unwraps `getOrThrow()`, passing strongly typed `SearchResult<Repository>` directly to Swift.

### 4. Summary Mapping Table

| Kotlin Symbol | Swift Symbol | Notes |
| :--- | :--- | :--- |
| `GithubCoreSdk.create()` | `GithubCoreSdk.companion.create()` | Access via the companion object factory. |
| `SearchRepositoriesUseCase` | `SearchRepositoriesUseCase(repository:)` | Initialized with `sdk.networkClient.apiService`. |
| `@Throws suspend fun search()` | `try await searchUseCase.search(query:)` | Suspending throwing method bridges directly to Swift `async/await`. |
| `Repository.description` | `repo.description_` | Renamed with trailing underscore to avoid collision with `NSObject.description`. |
| `SearchResult.items` | `result.items as? [Repository]` | Cast to native Swift typed array. |

---

## 💻 Implementation in Client App

The iOS app is organized into dedicated components matching the architecture of the Android sample:

```text
sample/sample-iOS/sample-iOS/
├── ContentView.swift             # Root entry view hosting the search screen
└── Search/
    ├── RepoSearchViewModel.swift # ObservableObject handling async SDK calls
    ├── RepoSearchScreen.swift    # SwiftUI screen container (search bar, states, list)
    └── RepositoryRow.swift       # Subview rendering individual repository cards
```

### 1. Root Host View
In [`ContentView.swift`](../sample/sample-iOS/sample-iOS/ContentView.swift):

```swift
import SwiftUI

struct ContentView: View {
    var body: some View {
        RepoSearchScreen()
    }
}
```

### 2. ViewModel with Swift Concurrency
In [`Search/RepoSearchViewModel.swift`](../sample/sample-iOS/sample-iOS/Search/RepoSearchViewModel.swift):

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

### 3. SwiftUI Search Screen
In [`Search/RepoSearchScreen.swift`](../sample/sample-iOS/sample-iOS/Search/RepoSearchScreen.swift):

```swift
struct RepoSearchScreen: View {
    @StateObject private var viewModel: RepoSearchViewModel

    init(searchUseCase: SearchRepositoriesUseCase? = nil) {
        _viewModel = StateObject(wrappedValue: RepoSearchViewModel(searchUseCase: searchUseCase))
    }

    var body: some View {
        NavigationStack {
            VStack(spacing: 16) {
                // Search bar
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

### 4. Rendering Domain Data
In [`Search/RepositoryRow.swift`](../sample/sample-iOS/sample-iOS/Search/RepositoryRow.swift):

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

## 🧪 Build Commands

```bash
# 1. Build the shared KMP framework for iOS Simulator
./gradlew :github-core:linkDebugFrameworkIosSimulatorArm64

# 2. Build the iOS sample application
cd sample/sample-iOS && xcodebuild -project sample-iOS.xcodeproj -scheme sample-iOS -destination 'generic/platform=iOS Simulator' build

# 3. Install & launch in a booted simulator (optional)
xcrun simctl install booted <path-to-sample-iOS.app>
xcrun simctl launch booted com.sample.ios.sample-iOS
```

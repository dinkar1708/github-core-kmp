# 🤖 Android KMP Usage & Sample App Guide

A concise guide to integrating the headless **`github-core-kmp`** SDK into an Android application, using [`sample/sample-android`](../sample/sample-android) as the reference.

---

## 💡 Core Concept: The Headless Boundary

The shared SDK engine provides business logic, data models, networking, and caching, while the Android application retains full control over native UI and presentation state:

* **SDK (`github-core-kmp`):** Domain models, validation rules, use cases, Ktor HTTP client (OkHttp engine), cache, and APM telemetry.
* **Android App (`sample-android`):** Jetpack Compose UI, coroutines, and Material 3 presentation state.
* **Strict Boundary:** No UI components, ViewModels, or platform presentation state inside the shared KMP engine.

---

## 🏛️ Packaging & Linking Type: Static vs. Dynamic

### Configuration
In [`github-core/build.gradle.kts`](../github-core/build.gradle.kts), the Android target is packaged as an **Android Archive (AAR)** using the Android Multiplatform Library plugin:

```kotlin
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
}

android {
    namespace = "com.github.core"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    minSdk = libs.versions.android.minSdk.get().toInt()
}
```

### Why Static?
* **Static Bytecode Compilation (DEXing):** All Kotlin/JVM code from the SDK and its submodules (`core-domain`, `core-network`, `core-cache`, `core-apm`) is compiled into bytecode (`.class`) and **statically compiled into Dalvik Executable (`.dex`) files** baked directly into the final APK (`classes.dex`).
* **Zero Dynamic C/C++ `.so` Overhead:** Pure Kotlin KMP modules on Android do not require native dynamic shared libraries (`.so`) or JNI runtime loading (`System.loadLibrary`). The code executes directly on the Android Runtime (ART) alongside host app code.
* **Whole-Program R8 Optimization:** Because the SDK is statically merged into the app's DEX graph, the Android R8 compiler performs cross-module dead-code elimination (tree shaking), optimization, and inlining during release builds (`minifyEnabled = true`).

### What if Dynamic?
* Unlike iOS (where dynamic Mach-O `.dylib` frameworks are common), Android dynamic code loading typically applies to native C/C++ libraries (`.so` via `System.loadLibrary`) or Dynamic Feature Modules (Play Feature Delivery). For shared KMP Kotlin libraries, static DEX packaging is the standard, safest, and most performant pattern.

---

## 📦 Build Setup & Configuration

### 1. Gradle Dependency
Add the SDK dependency to your application module ([`sample/sample-android/app/build.gradle.kts`](../sample/sample-android/app/build.gradle.kts)):

```kotlin
dependencies {
    implementation("com.github.core:github-core")
}
```

### 2. Composite Build Substitution
In [`sample/sample-android/settings.gradle.kts`](../sample/sample-android/settings.gradle.kts), link the local multiplatform project:

```kotlin
includeBuild("../../") {
    dependencySubstitution {
        substitute(module("com.github.core:github-core")).using(project(":github-core"))
    }
}
```
*(For remote consumption, replace the `includeBuild` block with your Maven/GitHub Packages repository credentials).*

### 3. Android Network Permission
In [`sample/sample-android/app/src/main/AndroidManifest.xml`](../sample/sample-android/app/src/main/AndroidManifest.xml):

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

---

## 🧹 SDK Clean & Rebuilding Lifecycle (`./gradlew clean`)

### Clean Behavior
* **Automatic Recompilation via Composite Builds:** Because Android utilizes Gradle Composite Builds (`includeBuild("../../")`), Gradle treats the KMP project as an active source dependency.
* If you run `./gradlew clean` in the root repository, all generated `build/` artifacts are wiped.
* When you next execute `./gradlew assembleDebug` in `sample-android`, Gradle automatically detects that the SDK artifacts are missing and recompiles the entire KMP engine on the fly.

### How to Rebuild
```bash
# Rebuild and assemble Android sample APK
cd sample/sample-android && ./gradlew assembleDebug
```
No manual linking or pre-compilation step is required for Android.

---

## 🔑 Key Definitions & Interop Notes

| Kotlin / KMP Component | Android Consumer Symbol | Role |
| :--- | :--- | :--- |
| `GithubCoreSdk.create()` | `GithubCoreSdk.create()` | Entrypoint factory providing `networkClient` and `cache`. |
| `SearchRepositoriesUseCase` | `SearchRepositoriesUseCase(repository:)` | Initialized with `sdk.networkClient.apiService`. |
| `suspend fun execute(query:)` | `searchUseCase.execute(query = query)` | Suspending method returning functional `Result<SearchResult<Repository>>`. |
| `Repository` | `repo.fullName`, `repo.description` | Immutable entity containing repository details, stars, and language. |
| `SearchResult.items` | `searchResult.items` | Strongly-typed Kotlin `List<Repository>`. |
| `DomainError` | `when (error) { ... }` | Sealed error types: `ValidationError`, `NetworkError`, `RateLimitExceededError`. |

---

## 💻 Implementation in Client App

The Android sample app is organized into dedicated components matching clean architecture boundaries:

```text
sample/sample-android/app/src/main/java/com/sample/android/
├── MainActivity.kt               # Root Activity hosting the search screen
└── search/
    ├── RepoSearchScreen.kt       # Screen container with search bar, states & LazyColumn
    └── RepositoryItem.kt         # Card component rendering individual repository items
```

### 1. Root Host View
In [`MainActivity.kt`](../sample/sample-android/app/src/main/java/com/sample/android/MainActivity.kt):

```kotlin
class MainActivity : ComponentActivity() {
    private val sdk by lazy { GithubCoreSdk.create() }
    private val searchUseCase by lazy { SearchRepositoriesUseCase(sdk.networkClient.apiService) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Sample_androidTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    RepoSearchScreen(
                        searchUseCase = searchUseCase,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
```

### 2. State & ViewModel Management
In [`RepoSearchScreen.kt`](../sample/sample-android/app/src/main/java/com/sample/android/search/RepoSearchScreen.kt):

```kotlin
var query by remember { mutableStateOf("kotlin") }
var isLoading by remember { mutableStateOf(false) }
var errorMessage by remember { mutableStateOf<String?>(null) }
var repositories by remember { mutableStateOf<List<Repository>>(emptyList()) }
val scope = rememberCoroutineScope()

fun performSearch(searchQuery: String) {
    if (searchQuery.isBlank()) return
    scope.launch {
        isLoading = true
        errorMessage = null
        val result = searchUseCase.execute(query = searchQuery.trim())
        result.onSuccess {
            repositories = it.items
            isLoading = false
        }.onFailure {
            errorMessage = it.message
            isLoading = false
        }
    }
}
```

### 3. Screen Container
In [`RepoSearchScreen.kt`](../sample/sample-android/app/src/main/java/com/sample/android/search/RepoSearchScreen.kt):

```kotlin
Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
    // Search input bar with action button
    Row(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(value = query, onValueChange = { query = it }, modifier = Modifier.weight(1f))
        Button(onClick = { performSearch(query) }) { Text("Search") }
    }

    // State presentation
    when {
        isLoading -> CircularProgressIndicator()
        errorMessage != null -> Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
        else -> LazyColumn {
            items(repositories, key = { it.id }) { repo ->
                RepositoryItem(repo = repo)
            }
        }
    }
}
```

### 4. Item Row Component
In [`RepositoryItem.kt`](../sample/sample-android/app/src/main/java/com/sample/android/search/RepositoryItem.kt):

```kotlin
@Composable
fun RepositoryItem(repo: Repository, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = repo.fullName, fontWeight = FontWeight.Bold)
            repo.description?.let { Text(text = it) }
            Text(text = "★ ${repo.stargazersCount} | ${repo.language ?: ""}")
        }
    }
}
```

---

## ⚡ Error Handling Patterns

Inspect `DomainError` subtypes to provide tailored user feedback:

```kotlin
result.onFailure { error ->
    when (error) {
        is DomainError.ValidationError -> showInputError(error.message)
        is DomainError.RateLimitExceededError -> showRateLimitBanner(error.resetTimeSeconds)
        is DomainError.NetworkError -> showRetryPrompt("Network error (${error.statusCode})")
        else -> showGenericError(error.message)
    }
}
```

---

## 🧪 Build & Run Commands

```bash
# 1. Build the Android sample application APK
cd sample/sample-android && ./gradlew assembleDebug

# 2. Install and launch on a connected device/emulator
./gradlew installDebug

# 3. Run all shared KMP tests across all modules
cd ../.. && ./gradlew check
```

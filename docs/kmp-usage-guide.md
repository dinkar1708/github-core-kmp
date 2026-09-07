# 📖 KMP Usage & Sample App Guide

A concise guide to integrating the headless **`github-core-kmp`** SDK into client applications, using [`sample/sample-android`](../sample/sample-android) as the reference.

---

## 💡 Core Concept: The Headless Boundary

The SDK provides business logic, data models, networking, and caching, while client applications retain full control over native UI and presentation state:

* **SDK (`github-core-kmp`):** Domain Models, Validation, Use Cases, Ktor HTTP Client, Cache, APM Telemetry.
* **App (`sample-android`):** Jetpack Compose UI, state management, and user interaction.
* **Strict Boundary:** No ViewModels, LiveData, or UI state classes inside the shared KMP engine.

---

## 📦 Import & Gradle Setup

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

## 🔑 Key Definitions & SDK Components

| Component | Package / Class | Role |
| :--- | :--- | :--- |
| **SDK Facade** | `com.github.core.GithubCoreSdk` | Entrypoint factory providing `networkClient` and `cache`. |
| **API Service** | `com.github.core.network.api.GithubApiService` | Ktor-backed remote data source implementing `GithubRepository`. |
| **Search Use Case** | `com.github.core.domain.usecase.SearchRepositoriesUseCase` | Validates query & pagination, executes repository search. |
| **Domain Model** | `com.github.core.domain.model.Repository` | Immutable entity containing repository details, stars, and owner. |
| **Error Hierarchy** | `com.github.core.domain.error.DomainError` | Sealed error types: `ValidationError`, `NetworkError`, `RateLimitExceededError`. |

---

## 💻 Usage in Client App

### 1. SDK & Use Case Initialization
In [`MainActivity.kt`](../sample/sample-android/app/src/main/java/com/sample/android/MainActivity.kt):

```kotlin
import com.github.core.GithubCoreSdk
import com.github.core.domain.usecase.SearchRepositoriesUseCase

// Initialize SDK and Search Use Case
val sdk = GithubCoreSdk.create()
val searchUseCase = SearchRepositoriesUseCase(sdk.networkClient.apiService)
```

### 2. Invoking the Use Case & Handling State
In [`RepoSearchScreen.kt`](../sample/sample-android/app/src/main/java/com/sample/android/search/RepoSearchScreen.kt):

```kotlin
// Execute asynchronously in coroutine scope
scope.launch {
    isLoading = true
    val result = searchUseCase.execute(query = searchQuery)

    result.onSuccess { searchResult ->
        repositories = searchResult.items  // List<Repository>
        isLoading = false
    }.onFailure { error ->
        errorMessage = error.message
        isLoading = false
    }
}
```

### 3. Rendering Domain Data
In [`RepositoryItem.kt`](../sample/sample-android/app/src/main/java/com/sample/android/search/RepositoryItem.kt), access domain properties directly:

```kotlin
Text(text = repo.fullName, fontWeight = FontWeight.Bold)
Text(text = repo.description ?: "")
Text(text = "★ ${repo.stargazersCount} | ${repo.language ?: ""}")
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

## 🧪 Build Commands

```bash
# Build Android sample APK
cd sample/sample-android && ./gradlew assembleDebug

# Run all shared KMP tests
./gradlew check
```

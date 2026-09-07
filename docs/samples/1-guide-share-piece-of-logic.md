# 🧩 1. Share a piece of logic — KMP Guide

> 🔗 **Official JetBrains Documentation:** [Choose what to share: Share a piece of logic](https://kotlinlang.org/multiplatform/#choose-share-what-piece-of-logic)

A comprehensive guide to adopting Kotlin Multiplatform by sharing an isolated piece of business logic, using [`sample/sample-share-piece-of-logic`](../../sample/sample-share-piece-of-logic) as the consumer reference.

---

## 💡 Core Concept: Isolated Business Logic

> [!NOTE]
> This architecture directly implements JetBrains' official Kotlin Multiplatform recommendation: [**"Stabilize and sync critical features" (`piece-of-logic`)**](https://kotlinlang.org/multiplatform/#choose-share-what-piece-of-logic) — starting by sharing an isolated, core part of business logic (validation rules, calculations, data models) to improve consistency across platforms without requiring major architectural changes.

### Architectural Boundary:
* **SDK Module (`:core-domain`):** Pure Kotlin business models, input validation algorithms (`QueryValidator`, `PaginationValidator`), and domain contracts. Zero third-party runtime dependencies (no Ktor, no SQLite, no UI).
* **Consumer App (`sample/sample-share-piece-of-logic`):** A lightweight client application importing **only** `:core-domain` to perform consistent client-side validation and data handling across platforms.
* **Strict SDK Purity:** The core SDK repository (`github-core-kmp`) has **zero knowledge** of the consumer sample. The sample lives independently in `sample/` and links the SDK via Gradle composite builds.

---

## 🏛️ Packaging & Linking Type: Static vs. Dynamic

### Configuration
The `:core-domain` module contains pure Kotlin multiplatform code with zero platform-specific dependencies:

```kotlin
// core-domain/build.gradle.kts
kotlin {
    androidTarget()
    iosArm64()
    iosSimulatorArm64()
    jvm()
}
```

### Why Static?
* **Zero Overhead:** Pure Kotlin domain classes compile directly into JVM bytecode (`.class`/`.dex`) on Android/JVM and native static libraries (`.a`) on iOS.
* **No Infrastructure Baggage:** By importing only `:core-domain`, the consumer application pulls in zero HTTP engines, zero database drivers, and zero serialization reflection. It is as lightweight as a standalone math or utility library.
* **Instant Compilation:** Because there are no native C/C++ dependencies or heavy frameworks, compilation is instantaneous.

### What if Dynamic?
* Standalone pure logic does not warrant dynamic linking (`.dylib` / `.so`). Using static compilation ensures that the R8 compiler (Android) and Clang dead-code strip (iOS) can eliminate unused domain models at compile time.

---

## 📦 Build Setup & Sample Location

### Proposed Sample Folder Location:
```text
github-core-kmp/
├── core-domain/                                # Pure SDK Module (unaware of any sample)
└── sample/
    ├── sample-share-piece-of-logic/            # Tier 1 Sample (Consumes only :core-domain)
    ├── sample-share-logic-native-ui-android/   # Tier 2 Android Sample (Native Jetpack Compose)
    ├── sample-share-logic-native-ui-ios/       # Tier 2 iOS Sample (Native SwiftUI)
    └── sample-share-both-logic-and-ui/         # Tier 3 Sample (Compose Multiplatform)
```

### 1. Gradle Setup (Composite Build)
In `sample/sample-share-piece-of-logic/settings.gradle.kts`:

```kotlin
includeBuild("../../") {
    dependencySubstitution {
        substitute(module("com.github.core:core-domain")).using(project(":core-domain"))
    }
}
```

In `sample/sample-share-piece-of-logic/build.gradle.kts`:

```kotlin
dependencies {
    // Import ONLY the isolated business logic module:
    implementation("com.github.core:core-domain")
}
```

---

## 🧹 SDK Clean & Rebuilding Lifecycle (`./gradlew clean`)

### Clean Behavior
* Gradle tracks the composite build dependency automatically.
* When `./gradlew clean` is executed in the root repository, `:core-domain` artifacts are wiped.
* Running tests or compiling in `sample-share-piece-of-logic` triggers an automatic, instant re-compilation of `:core-domain` on demand.

### How to Rebuild
```bash
cd sample/sample-share-piece-of-logic && ./gradlew test
```

---

## 🔑 Key Definitions & Domain Rules

The consumer accesses isolated validation logic and pure domain entities without any networking:

| Component | Package / Symbol | Role |
| :--- | :--- | :--- |
| **`QueryValidator`** | `com.github.core.domain.validation.QueryValidator` | Sanitizes queries, enforces non-blank input, and validates maximum query length (`maxQueryLength = 100`). |
| **`PaginationValidator`** | `com.github.core.domain.validation.PaginationValidator` | Enforces valid page numbering (`page >= 1`) and bounds check (`perPage in 1..100`). |
| **`Repository`** | `com.github.core.domain.model.Repository` | Immutable entity containing repository identifiers, star counts, and language info. |
| **`DomainError.ValidationError`** | `com.github.core.domain.error.DomainError.ValidationError` | Fast-fail domain exception returning field-specific validation errors. |

---

## 💻 Implementation in Client App

### 1. Shared Validation in Client Code

```kotlin
import com.github.core.domain.validation.QueryValidator
import com.github.core.domain.validation.PaginationValidator

class RegistrationFormValidator {
    private val queryValidator = QueryValidator()
    private val paginationValidator = PaginationValidator()

    fun validateUserInput(rawInput: String): Boolean {
        val result = queryValidator.validate(rawInput)
        return result.isSuccess
    }

    fun validatePageBounds(page: Int, pageSize: Int): Boolean {
        val result = paginationValidator.validate(page = page, perPage = pageSize)
        return result.isSuccess
    }
}
```

### 2. Swift Interop for iOS

When `:core-domain` is linked in iOS, Swift consumes the validation logic directly:

```swift
import GithubCoreKMP

let validator = QueryValidator()
let validationResult = validator.validate(query: userInput)

if validationResult.isSuccess {
    let cleanQuery = validationResult.getOrNull() ?? ""
    print("Valid query: \(cleanQuery)")
} else {
    print("Invalid search query")
}
```

---

## ⚡ Error Handling Patterns

Inspect `DomainError.ValidationError` for field-level form validation:

```kotlin
val result = queryValidator.validate(rawInput)

result.onSuccess { cleanQuery ->
    proceedWithSearch(cleanQuery)
}.onFailure { error ->
    when (error) {
        is DomainError.ValidationError -> {
            highlightInputField(field = error.field, message = error.message)
        }
        else -> displayGenericError(error.message)
    }
}
```

---

## 🧪 Build & Run Commands

```bash
# Run unit tests verifying isolated domain logic
cd sample/sample-share-piece-of-logic && ./gradlew test

# Run multiplatform domain verification in root SDK
./gradlew :core-domain:allTests
```

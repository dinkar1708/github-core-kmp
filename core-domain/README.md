# 📦 `:core-domain`

The **pure domain & business logic core** of the GitHub KMP SDK engine.

---

## 🎯 Architectural Responsibility & Boundary

* **Zero Infrastructure Dependencies:** Has no knowledge of HTTP (Ktor), SQLite, or UI frameworks.
* **Domain Models:** Immutably modeled core business entities (`Repository`, `User`, `SearchResult`, `Owner`, `License`).
* **Validation & Sorting Rules:** Pure input validation (e.g. query sanitization, pagination bound checks) and sorting/filtering rules.
* **Use Cases:** Declarative, pure business operations (e.g. `SearchRepositoriesUseCase`, `GetRepositoryDetailUseCase`, `GetUserRepositoriesUseCase`).
* **Repository Interfaces:** Inverted interfaces (`RepositorySearchRepository`, etc.) implemented by `:core-network` / `:core-cache`.

---

## 🏗️ Structure

```text
core-domain/
├── src/
│   ├── commonMain/kotlin/com/github/core/domain/
│   │   ├── model/         # Domain entities (Repository, User, etc.)
│   │   ├── repository/    # Domain repository interfaces (Contracts)
│   │   ├── usecase/       # Pure Use Cases (SearchRepositoriesUseCase, etc.)
│   │   ├── validation/    # Query & pagination validation rules
│   │   └── error/         # Domain error hierarchy
│   └── commonTest/        # Multiplatform Unit Tests for Use Cases & validations
└── build.gradle.kts
```

---

## 🧪 Running Tests

### 1. Standard Run (uses Gradle cache):
```bash
./gradlew :core-domain:allTests
```

### 2. Force Re-run & Print Live Logs (bypasses cache):
```bash
./gradlew :core-domain:allTests --rerun-tasks
```

### 3. Target-Specific Test Commands:
```bash
# Android Host / JVM unit tests
./gradlew :core-domain:testAndroidHostTest --rerun-tasks

# iOS Simulator unit tests
./gradlew :core-domain:iosSimulatorArm64Test --rerun-tasks
```

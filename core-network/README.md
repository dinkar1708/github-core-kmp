# 🌐 `:core-network`

The **resilient HTTP networking engine** for the GitHub KMP SDK powered by Ktor Client 3.x.

---

## 🎯 Architectural Responsibility & Boundary

* **Ktor 3.x Client:** Configures engine, serialization (`kotlinx.serialization`), and logging.
* **GitHub REST API Client:** Endpoints for `/search/repositories`, `/repos/{owner}/{repo}`, `/users/{username}/repos`.
* **Enterprise Resilience:**
  * **Rate Limiting Handling:** Inspects GitHub `x-ratelimit-remaining` / `x-ratelimit-reset` headers and exposes rate-limit backpressure.
  * **Circuit Breaker:** Prevents cascading network failures when GitHub API is degraded.
  * **Exponential Backoff & Retry Interceptor:** Automatic retry mechanism for idempotent HTTP GET requests.
* **Data-to-Domain Mapping:** Converts Network DTOs to pure `:core-domain` models.

---

## 🏗️ Structure

```text
core-network/
├── src/
│   ├── commonMain/kotlin/com/github/core/network/
│   │   ├── api/           # GitHub REST API client & routes
│   │   ├── dto/           # Network Data Transfer Objects (DTOs)
│   │   ├── mapper/        # DTO -> Domain Model mappers
│   │   ├── resilience/    # Circuit Breaker, Retry Policy, Rate Limiter
│   │   └── client/        # Ktor HttpClient factory & configuration
│   └── commonTest/        # MockEngine integration tests for API & resilience
└── build.gradle.kts
```

---

## 🧪 Running Tests

### 1. Standard Run (uses Gradle cache):
```bash
./gradlew :core-network:allTests
```

### 2. Force Re-run & Print Live Logs (bypasses cache):
```bash
./gradlew :core-network:allTests --rerun-tasks
```

### 3. Target-Specific Test Commands:
```bash
# Android Host / JVM network tests
./gradlew :core-network:testAndroidHostTest --rerun-tasks

# iOS Simulator network tests
./gradlew :core-network:iosSimulatorArm64Test --rerun-tasks
```

# 🌐 `:core-network`

The **resilient HTTP networking engine** for the GitHub KMP SDK powered by Ktor Client 3.x.

---

## 🎯 Architectural Responsibility & Boundary

* **Ktor 3.x Client:** Configures platform engines (`OkHttp` on Android/JVM, `Darwin` on iOS), serialization (`kotlinx.serialization`), and logging.
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
│   │   ├── api/           # GitHub REST API client & routes (GithubApiService)
│   │   ├── client/        # Ktor HttpClient factory & platform engines
│   │   ├── dto/           # Network Data Transfer Objects (DTOs)
│   │   └── mapper/        # DTO -> Domain Model mappers
│   ├── androidMain/       # Android OkHttp engine configuration
│   ├── iosMain/           # iOS Darwin (NSURLSession) engine configuration
│   └── commonTest/kotlin/com/github/core/network/
│       ├── NetworkTest.kt                    # Offline MockEngine tests (CI/CD)
│       └── LiveGithubApiIntegrationTest.kt   # Real live GitHub API tests
└── build.gradle.kts
```

---

## 🧪 Testing: Mock Tests vs. Real Live API Calls

### 1. 🛡️ Offline / Mock Tests (for CI/CD & Fast Local Runs)
Uses Ktor `MockEngine` to test API routes, JSON deserialization, and HTTP 403 Rate Limit handling in memory with zero internet requirement:
```bash
./gradlew :core-network:allTests --rerun-tasks
```

---

### 2. 📡 Real Live API Call (Connects directly to `api.github.com`)
Sends an actual live HTTP GET request to GitHub servers, deserializes real repositories, and logs live stars and metadata to the console:
```bash
./gradlew :core-network:testAndroidHostTest --rerun-tasks
```

> **Note on CI/CD:**  
> In [`LiveGithubApiIntegrationTest.kt`](./src/commonTest/kotlin/com/github/core/network/LiveGithubApiIntegrationTest.kt), uncomment `@Ignore` before pushing to CI/CD pipelines to prevent burning GitHub's unauthenticated 60 req/hour limit on automated runners.

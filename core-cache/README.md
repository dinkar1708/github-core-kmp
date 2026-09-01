# 💾 `:core-cache`

The **offline-first local persistence & caching engine** for the GitHub KMP SDK.

---

## 🎯 Architectural Responsibility & Boundary

* **Offline Persistence:** Provides persistent local caching for repository searches, user bookmarks, and recent queries.
* **Cache Eviction & TTL Policies:** Enforces Time-To-Live (TTL) freshness checks and Least-Recently-Used (LRU) cache invalidation.
* **Cold-Start Acceleration:** Instant data availability from local SQLite storage while fresh data is synced in the background.
* **Cache-to-Domain Mapping:** Converts database entities/DTOs into pure `:core-domain` models.

---

## 🏗️ Structure

```text
core-cache/
├── src/
│   ├── commonMain/kotlin/com/github/core/cache/
│   │   ├── database/      # SQLDelight / SQLite database driver factory
│   │   ├── entity/        # Cache entity representations
│   │   ├── policy/        # TTL & eviction policies
│   │   ├── mapper/        # Cache Entity -> Domain Model mappers
│   │   └── datasource/    # Local cache data sources
│   └── commonTest/        # In-memory database integration tests
└── build.gradle.kts
```

---

## 🧪 Running Tests

### 1. Standard Run (uses Gradle cache):
```bash
./gradlew :core-cache:allTests
```

### 2. Force Re-run & Print Live Logs (bypasses cache):
```bash
./gradlew :core-cache:allTests --rerun-tasks
```

### 3. Target-Specific Test Commands:
```bash
# Android Host / JVM cache tests
./gradlew :core-cache:testAndroidHostTest --rerun-tasks

# iOS Simulator cache tests
./gradlew :core-cache:iosSimulatorArm64Test --rerun-tasks
```

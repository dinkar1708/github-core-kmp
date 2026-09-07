# 💾 `:core-cache`

Offline-first caching and local persistence engine for the GitHub Core KMP SDK.

---

## 💡 Core Concept & Architectural Role

* **Cold-Start Acceleration:** Delivers instant UI rendering (0ms initial latency) from local storage while fresh network data syncs in the background.
* **Offline Usability:** Preserves search queries and repository listings across app restarts and network dropouts.
* **Storage Abstraction:** Decoupled persistence interface providing in-memory key-value caching today with an architectural path to multiplatform SQLite (SQLDelight/Room KMP).

---

## 📦 Import & Dependencies

### Gradle
```kotlin
dependencies {
    // Consumer applications import the umbrella SDK:
    implementation("com.github.core:github-core")
    // Or internal module dependency:
    implementation(project(":core-cache"))
}
```

### Key Kotlin Imports
```kotlin
import com.github.core.cache.GithubCache
import com.github.core.domain.model.Repository
```

---

## 🔑 Key Definitions: `GithubCache`

`GithubCache` (`com.github.core.cache`) provides thread-safe in-memory caching keyed by query strings:

| Method | Definition & Purpose |
| :--- | :--- |
| `save(query: String, repositories: List<Repository>)` | Stores domain repositories associated with a given search query key. |
| `get(query: String): List<Repository>?` | Returns cached repositories in $\mathcal{O}(1)$ time, or `null` on cache miss. |
| `clear()` | Purges all cached query entries from memory. |

---

## 🔬 Internal Mechanics & Orchestration Patterns

### 1. In-Memory Storage Mapping
```text
Normalized Query Key           Cached Domain Entities
"kotlin"                ──►    List<Repository> [id=1, id=2, ...]
"compose"               ──►    List<Repository> [id=3, id=4, ...]
```

### 2. Cache-First with Network Fallback Pattern
```text
Search Request ──► [ Check Cache ] ── (Hit?) ──► Return Cached Data (0ms)
                          │
                          ▼ (Miss)
                   [ Query Network ] ──► Populate Cache ──► Return Remote Data
```

### 3. Stale-While-Revalidate (SWR) Pattern
1. Emit cached results immediately to the UI.
2. Trigger asynchronous background sync via `:core-network`.
3. Update cache with fresh response and emit updated list to UI.

---

## 💻 Usage Pattern

```kotlin
val cache = GithubCache()

// 1. Read from cache for immediate rendering
val cached = cache.get("kotlin")
if (cached != null) {
    displayRepositories(cached)
}

// 2. Persist fresh network results
cache.save("kotlin", freshRepositories)
```

---

## 🧪 Verification

```bash
# Run multiplatform cache tests
./gradlew :core-cache:allTests --rerun-tasks
```

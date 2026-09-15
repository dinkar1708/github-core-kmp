# Module Spec: `:core-cache`

> 📖 **Official Standards & References:**  
> • [Kotlinx Coroutines: Shared Mutable State & Mutex](https://kotlinlang.org/docs/shared-mutable-state-and-coroutines.html#mutual-exclusion)  
> • [Microsoft Cloud Design Patterns: Cache-Aside Pattern](https://learn.microsoft.com/en-us/azure/architecture/patterns/cache-aside)  
> • [IETF RFC 7234: Hypertext Transfer Protocol (HTTP/1.1): Caching](https://datatracker.ietf.org/doc/html/rfc7234)

## 💡 Architectural Role & Concept
* **Cold-Start Acceleration:** Delivers instant UI rendering (sub-1ms initial latency) from local memory while fresh network data syncs in the background.
* **Offline Usability:** Preserves search queries and repository listings across active user sessions.
* **Thread-Safe Storage:** Uses Kotlin Coroutines `Mutex` to serialize reads and writes across multiplatform threads without blocking JVM or Apple OS threads.

---

## 🔑 Key Definitions: `GithubCache`

`GithubCache` (`com.github.core.cache`) provides thread-safe in-memory caching keyed by query strings:

| Method | Definition & Purpose |
| :--- | :--- |
| `save(query: String, repositories: List<Repository>)` | Stores domain repositories associated with a normalized query key. |
| `get(query: String): List<Repository>?` | Returns cached repositories in $\mathcal{O}(1)$ time, or `null` on cache miss or TTL expiration. |
| `isFresh(query: String): Boolean` | Checks whether the cached entry is within its 5-minute freshness window. |
| `remove(query: String)` | Evicts a specific query entry from memory. |
| `clear()` | Purges all cached query entries from memory. |
| `size(): Int` | Returns the current count of cached query keys. |

---

## 🔬 Cache Orchestration Policies

```mermaid
flowchart TD
    REQ["Cache Request: get(query)"] --> NORM["1. Normalize Query Key<br/>rawKey.trim().lowercase()"]
    NORM --> MUTEX["2. Acquire Coroutine Mutex<br/>Thread-Safe Non-Blocking Lock"]
    MUTEX --> LOOKUP{"3. Entry Exists in LinkedHashMap?"}
    
    LOOKUP -->|No (Miss)| MISS["Return null<br/>(Trigger Remote Network Fetch)"]
    LOOKUP -->|Yes| TTL{"4. Has TTL Expired?<br/>now - timestamp > 5 mins"}
    
    TTL -->|Expired| EVICT["Evict Stale Entry<br/>Return null"]
    TTL -->|Fresh| HIT["Return Cached List&lt;Repository&gt;<br/>(&lt; 1ms Instant Response)"]

    style REQ fill:#f8f9fa,stroke:#495057,stroke-width:1px,color:#000
    style NORM fill:#e7f5ff,stroke:#1c7ed6,stroke-width:1px,color:#000
    style MUTEX fill:#f3d9fa,stroke:#ae3ec9,stroke-width:1px,color:#000
    style LOOKUP fill:#fff3bf,stroke:#f08c00,stroke-width:1px,color:#000
    style TTL fill:#fff3bf,stroke:#f08c00,stroke-width:1px,color:#000
    style HIT fill:#d3f9d8,stroke:#2b8a3e,stroke-width:2px,color:#000
    style MISS fill:#ffe3e3,stroke:#e03131,stroke-width:1px,color:#000
    style EVICT fill:#ffe3e3,stroke:#e03131,stroke-width:1px,color:#000
```

### 1. In-Memory Key Normalization
Raw queries are sanitized before lookup or insertion:
$$\text{key} = \text{rawKey.trim().lowercase()}$$
Ensures that `"Kotlin"`, `"kotlin "`, and `"  KOTLIN"` resolve to the same cache entry.

### 2. Time-To-Live (TTL) Freshness
* Default duration: 5 minutes (`DEFAULT_TTL_MS = 300,000L`).
* Timestamps are recorded using `Clock.System.now().toEpochMilliseconds()`.
* When an entry is read past its TTL, it is immediately purged and `null` is returned.

### 3. Capacity Bounding & LRU Eviction
* Default capacity: 100 entries (`DEFAULT_MAX_CAPACITY = 100`).
* Backed by a `LinkedHashMap`. When an insertion exceeds capacity, the oldest entry (FIFO/insertion order) is automatically evicted to ensure memory safety.

---

## 💻 Kotlin Usage Pattern

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

## 🧪 Testing Strategy
* Tests concurrent multi-threaded reads/writes using Kotlin Coroutines `joinAll` and `async`.
* Validates TTL expiration by injecting controlled clock timestamps.
* Validates capacity enforcement by inserting 105 entries and asserting `size() == 100`.

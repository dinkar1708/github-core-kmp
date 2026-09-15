package com.github.core.cache

import com.github.core.domain.model.Repository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock

data class CacheEntry<T>(
    val data: T,
    val timestampMillis: Long
) {
    fun isExpired(nowMillis: Long, ttlMillis: Long): Boolean =
        nowMillis - timestampMillis > ttlMillis
}

/**
 * Thread-safe multiplatform in-memory cache for GitHub domain entities.
 * Enforces TTL freshness policies and LRU eviction to prevent unbounded memory growth.
 */
class GithubCache(
    val ttlMillis: Long = DEFAULT_TTL_MS,
    val maxCapacity: Int = DEFAULT_MAX_CAPACITY,
    private val clock: Clock = Clock.System
) {
    private val mutex = Mutex()
    private val memoryStore = LinkedHashMap<String, CacheEntry<List<Repository>>>()

    suspend fun save(query: String, repositories: List<Repository>) = mutex.withLock {
        val key = normalizeKey(query)
        if (memoryStore.size >= maxCapacity && !memoryStore.containsKey(key)) {
            // Evict oldest entry (FIFO / LRU insertion order)
            val oldestKey = memoryStore.keys.firstOrNull()
            if (oldestKey != null) {
                memoryStore.remove(oldestKey)
            }
        }
        memoryStore[key] = CacheEntry(
            data = repositories,
            timestampMillis = clock.now().toEpochMilliseconds()
        )
    }

    suspend fun get(query: String): List<Repository>? = mutex.withLock {
        val key = normalizeKey(query)
        val entry = memoryStore[key] ?: return null
        val now = clock.now().toEpochMilliseconds()
        if (entry.isExpired(now, ttlMillis)) {
            memoryStore.remove(key)
            return null
        }
        entry.data
    }

    suspend fun isFresh(query: String): Boolean = mutex.withLock {
        val key = normalizeKey(query)
        val entry = memoryStore[key] ?: return false
        val now = clock.now().toEpochMilliseconds()
        !entry.isExpired(now, ttlMillis)
    }

    suspend fun remove(query: String) = mutex.withLock {
        memoryStore.remove(normalizeKey(query))
    }

    suspend fun clear() = mutex.withLock {
        memoryStore.clear()
    }

    suspend fun size(): Int = mutex.withLock {
        memoryStore.size
    }

    private fun normalizeKey(rawKey: String): String = rawKey.trim().lowercase()

    companion object {
        const val DEFAULT_TTL_MS = 5 * 60 * 1000L // 5 minutes
        const val DEFAULT_MAX_CAPACITY = 100
    }
}

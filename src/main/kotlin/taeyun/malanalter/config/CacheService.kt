package taeyun.malanalter.config

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}
@Service
class CacheService (
    private val cacheManager: CacheManager,
){
    fun <T> cacheable(cacheName: String, key: Any, func: () -> T): T {
        val cache = cacheManager.getCache(cacheName)
            ?: throw IllegalStateException("Cache $cacheName not found")

        return try {
            // Try to get from cache
            @Suppress("UNCHECKED_CAST")
            cache.get(key){
                func()
            } as T
        } catch (e: Exception) {
            logger.error(e) { "❌ Cache error for key: $key in cache: $cacheName, evicting and regenerating" }
            // On error, evict the corrupted cache entry and regenerate
            cache.evict(key)
            func()
        }
    }
}
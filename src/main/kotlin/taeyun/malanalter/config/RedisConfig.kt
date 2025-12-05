package taeyun.malanalter.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.cache.CacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import taeyun.malanalter.config.property.RedisProperties
import java.time.Duration

@Configuration
class RedisConfig(
    private val redisProperties: RedisProperties,
) {

    /**
     * Redis connection factory using Lettuce client
     */
    @Bean
    fun redisConnectionFactory(): LettuceConnectionFactory {
        return LettuceConnectionFactory(redisProperties.host, redisProperties.port)
    }

    /**
     * RedisTemplate for String key-value pairs
     * Used for JSON storage (talent pool, invitations, party cache, etc.)
     *
     * Both key and value use StringRedisSerializer:
     * - Keys: "talent:123:user:456"
     * - Values: JSON strings (e.g., '{"userId":100,"characterId":"abc"}')
     */
    @Bean
    fun redisTemplate(): RedisTemplate<String, String> {
        val template = RedisTemplate<String, String>()
        template.connectionFactory = redisConnectionFactory()

        // Serialize both keys and values as strings
        template.keySerializer = StringRedisSerializer()
        template.valueSerializer = StringRedisSerializer()
        template.hashKeySerializer = StringRedisSerializer()
        template.hashValueSerializer = StringRedisSerializer()

        return template
    }

    /**
     * Redis message listener container
     * Manages Redis pub/sub subscriptions for keyspace events
     */
    @Bean
    fun redisMessageListenerContainer(): RedisMessageListenerContainer {
        return RedisMessageListenerContainer().apply {
            setConnectionFactory(redisConnectionFactory())
        }
    }

    @Bean
    fun redisSerializer(): RedisSerializer<Any> {
        val objectMapper = ObjectMapper().registerKotlinModule()
            .registerModule(JavaTimeModule())
            .activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                    .allowIfBaseType(Any::class.java).build(), ObjectMapper.DefaultTyping.EVERYTHING)
        return GenericJackson2JsonRedisSerializer(objectMapper)
    }

    /**
     * Cache manager using JSON serialization for values
     * Creates a copy of ObjectMapper with type information enabled
     */
    @Bean
    fun cacheManager(): CacheManager {

        // Configure cache with JSON serialization
        val config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(1)) // Cache TTL: 1 hour
            .disableCachingNullValues() // Don't cache null values
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer())
            )
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(redisSerializer())
            )

        return RedisCacheManager.builder(redisConnectionFactory())
            .cacheDefaults(config)
            .build()
    }
}

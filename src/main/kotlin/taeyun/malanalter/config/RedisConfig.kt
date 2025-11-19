package taeyun.malanalter.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.serializer.StringRedisSerializer
import taeyun.malanalter.config.property.RedisProperties

@Configuration
class RedisConfig(private val redisProperties: RedisProperties) {

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
}

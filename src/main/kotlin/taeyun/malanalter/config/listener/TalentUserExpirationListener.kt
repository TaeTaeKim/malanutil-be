package taeyun.malanalter.config.listener

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

/**
 * Listens for Redis key expiration events for talent user keys (talent:user:*)
 *
 * When a talent:user:{userId} key expires, it indicates the user has not renewed
 * their registration in the talent pool within the TTL period (15 minutes).
 */
@Component
class TalentUserExpirationListener(
    redisMessageListenerContainer: RedisMessageListenerContainer
) : KeyExpirationEventMessageListener(redisMessageListenerContainer) {

    companion object {
        private const val TALENT_USER_PREFIX = "talent:user:"
    }

    /**
     * Called when a Redis key expires
     * Filters for talent user keys and processes them
     */
    override fun onMessage(message: Message, pattern: ByteArray?) {
        // Get the expired key name
        val expiredKey = message.toString()

        // Check if this is a talent user key
        if (isTalentUserKey(expiredKey)) {
            handleTalentUserExpiration(expiredKey)
        }
    }

    /**
     * Checks if the expired key matches the talent user pattern
     * Pattern: talent:user:{userId}
     */
    private fun isTalentUserKey(key: String): Boolean {
        return key.startsWith(TALENT_USER_PREFIX)
    }

    /**
     * Handles the expiration of a talent user key
     * Extract the userId and implement your business logic here
     */
    private fun handleTalentUserExpiration(expiredKey: String) {
        // Extract userId from key (format: talent:user:{userId})
        val userId = extractUserId(expiredKey)

        logger.info { "Talent user registration expired for userId: $userId (key: $expiredKey)" }

        // TODO: Implement your business logic here
        // Examples:
        // 1. Remove user from talent pool in cache
        // 2. Clean up related talent pool entries (talent:{talentId}:users set)
        // 3. Send notification to user about expiration
        // 4. Update user status in database if needed
        // 5. Log analytics about user activity duration
    }

    /**
     * Extracts userId from the talent user key
     * Example: "talent:user:12345" -> "12345"
     */
    private fun extractUserId(key: String): String {
        return key.removePrefix(TALENT_USER_PREFIX)
    }
}
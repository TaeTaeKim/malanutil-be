package taeyun.malanalter.config.listener

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

/**
 * Listens for Redis key expiration events for party heartbeat keys (party:*:heartbeat)
 *
 * When a party heartbeat key expires, it indicates the party leader has not renewed
 * the heartbeat within the TTL period, suggesting the party may be inactive.
 */
@Component
class PartyHeartbeatExpirationListener(
    redisMessageListenerContainer: RedisMessageListenerContainer
) : KeyExpirationEventMessageListener(redisMessageListenerContainer) {

    companion object {
        private const val PARTY_HEARTBEAT_PATTERN = "party:"
        private const val HEARTBEAT_SUFFIX = ":heartbeat"
    }

    /**
     * Called when a Redis key expires
     * Filters for party heartbeat keys and processes them
     */
    override fun onMessage(message: Message, pattern: ByteArray?) {
        // Get the expired key name
        val expiredKey = message.toString()

        // Check if this is a party heartbeat key
        if (isPartyHeartbeatKey(expiredKey)) {
            handlePartyHeartbeatExpiration(expiredKey)
        }
    }

    /**
     * Checks if the expired key matches the party heartbeat pattern
     * Pattern: party:{partyId}:heartbeat
     */
    private fun isPartyHeartbeatKey(key: String): Boolean {
        return key.startsWith(PARTY_HEARTBEAT_PATTERN) && key.endsWith(HEARTBEAT_SUFFIX)
    }

    /**
     * Handles the expiration of a party heartbeat key
     * Extract the partyId and implement your business logic here
     */
    private fun handlePartyHeartbeatExpiration(expiredKey: String) {
        // Extract partyId from key (format: party:{partyId}:heartbeat)
        val partyId = extractPartyId(expiredKey)

        logger.info { "Party heartbeat expired for partyId: $partyId (key: $expiredKey)" }

        // TODO: Implement your business logic here
        // Examples:
        // 1. Mark party as inactive in database
        // 2. Send notification to party members
        // 3. Clean up related Redis keys
        // 4. Update party status
        // 5. Trigger Discord notifications
    }

    /**
     * Extracts partyId from the heartbeat key
     * Example: "party:abc123:heartbeat" -> "abc123"
     */
    private fun extractPartyId(key: String): String {
        return key.removePrefix(PARTY_HEARTBEAT_PATTERN)
            .removeSuffix(HEARTBEAT_SUFFIX)
    }
}
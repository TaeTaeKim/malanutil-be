package taeyun.malanalter.config.listener

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.updateReturning
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.stereotype.Component
import taeyun.malanalter.party.pat.dao.PartyTable
import taeyun.malanalter.party.pat.service.PartyRedisService

private val logger = KotlinLogging.logger {}

/**
 * Listens for Redis key expiration events for party heartbeat keys (party:*:heartbeat)
 *
 * When a party heartbeat key expires, it indicates the party leader has not renewed
 * the heartbeat within the TTL period, suggesting the party may be inactive.
 */
@Component
class PartyHeartbeatExpirationListener(
    redisMessageListenerContainer: RedisMessageListenerContainer,
    private val partyRedisService: PartyRedisService,
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

        val partyRow = transaction {
            PartyTable.updateReturning(where = { PartyTable.id eq partyId }) {
                it[PartyTable.inactiveSince] = CurrentDateTime
            }.singleOrNull() ?: throw IllegalStateException("Party id $partyId not found")
        }
        // 파티 비활성화 웹소켓 알림 전송
        partyRedisService.publishMessage(
            PartyRedisService.partyDeleteTopic(partyRow[PartyTable.mapId]),
            hashMapOf("partyId" to partyId, "type" to "DEACTIVATED")
        )

        logger.info { "Party heartbeat expired for partyId: $partyId (key: $expiredKey)" }
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
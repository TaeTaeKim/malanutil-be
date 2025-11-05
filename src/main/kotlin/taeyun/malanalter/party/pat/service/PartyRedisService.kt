package taeyun.malanalter.party.pat.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class PartyRedisService(
    val redisTemplate: RedisTemplate<String, String>,
    val objectMapper: ObjectMapper
) {

    companion object {
        private const val PARTY_TIMEOUT_MINUTES = 15L
        private const val PARTY_KEY_PREFIX = "party"

        fun getPartyHeartbeatKey(partyId: String): String {
            return "$PARTY_KEY_PREFIX:$partyId:heartbeat"
        }
    }


    fun registerPartyHeartbeat(partyId: String) {
        val partyHeartbeatKey = getPartyHeartbeatKey(partyId)
        redisTemplate.opsForValue().set(
            partyHeartbeatKey,
            "",
            PARTY_TIMEOUT_MINUTES,
            TimeUnit.MINUTES
        )

    }

    fun getPartyTTL(partyId: String) : Long{
        val partyHeartbeatKey = getPartyHeartbeatKey(partyId)
        return redisTemplate.getExpire(partyHeartbeatKey)
    }

    fun removePartyHeartbeat(partyId: String) {
        val partyHeartbeatKey = getPartyHeartbeatKey(partyId)
        redisTemplate.delete(partyHeartbeatKey)
    }
}
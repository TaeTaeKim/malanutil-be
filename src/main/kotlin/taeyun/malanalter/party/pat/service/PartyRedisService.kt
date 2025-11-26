package taeyun.malanalter.party.pat.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import taeyun.malanalter.party.pat.dto.DiscordMessageDto
import java.util.concurrent.TimeUnit

@Service
class PartyRedisService(
    val redisTemplate: RedisTemplate<String, String>,
    val objectMapper: ObjectMapper
) {

    companion object {
        private const val PARTY_TIMEOUT_MINUTES = 1L
        private const val PARTY_KEY_PREFIX = "party"

        fun getPartyHeartbeatKey(partyId: String): String {
            return "$PARTY_KEY_PREFIX:$partyId:heartbeat"
        }
        fun getMapDiscordKey(mapId: Long): String {
            return "discord:messages:$mapId"
        }
        fun partyCreateTopic(mapId:Long) : String{
            return "party:$mapId:create"
        }
        fun partyUpdateTopic(mapId:Long) : String{
            return "party:$mapId:update"
        }
        fun partyDeleteTopic(mapId:Long) : String{
            return "party:$mapId:delete"
        }
        fun talentRegisterTopic(mapId:Long) : String{
            return "talent:$mapId:register"
        }
        fun talentUnRegisterTopic(mapId:Long) : String{
            return "talent:$mapId:unregister"
        }
        // todo:  Party manage 토픽으로 변경해야한다. -> 파티장이 구독해서 참여한 파티원의 state를 실시간으로 받을 수 있는 토픽
        fun partyApplyTopic(partyId: String) : String{
            return "party:apply:$partyId"
        }
        fun partyInviteTopic(inviteUserId: String) : String{
            return "finder:invite:$inviteUserId"
        }
        fun partyAcceptedTopic(acceptedUserId: Long) : String{
            return "finder:accept:$acceptedUserId"
        }
        fun partyLeaveTopic(leaveUserId: Long) : String{
            return "finder:leave:$leaveUserId"
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
    fun getDiscordOfMaps(mapIds: List<Long>): Map<Long, List<DiscordMessageDto>> {
        val result = mutableMapOf<Long, List<DiscordMessageDto>>()
        mapIds.forEach { mapId ->
            val key = getMapDiscordKey(mapId)
            val jsonStrings = redisTemplate.opsForZSet().range(key, 0, -1)
            val messages = jsonStrings?.mapNotNull { jsonString ->
                try {
                    objectMapper.readValue(jsonString, DiscordMessageDto::class.java)
                } catch (e: Exception) {
                    // Log error and skip invalid entries
                    logger.error{ "Json Deserialization error of $jsonString" }
                    null
                }
            }?.sortedByDescending { it.timestamp }
            result.put(mapId, messages?: emptyList())
        }
        return result
    }

    fun publishMessage(topic: String, message: Any){
        val data = objectMapper.writeValueAsString(message)
        redisTemplate.convertAndSend(topic, data)
    }
}
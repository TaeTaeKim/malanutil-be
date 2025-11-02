package taeyun.malanalter.party.pat

import com.fasterxml.jackson.databind.ObjectMapper
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import taeyun.malanalter.config.exception.ErrorCode
import taeyun.malanalter.config.exception.PartyBadRequest
import taeyun.malanalter.party.character.CharacterEntity
import taeyun.malanalter.party.pat.dto.RegisteringPoolResponse
import taeyun.malanalter.party.pat.dto.TalentDto
import taeyun.malanalter.user.UserService
import java.util.concurrent.TimeUnit

/**
 * 메랜 인재풀 관리를 위한 Redis Talent 서비스
 *
 * talent:{mapId}:users -> 15분 TTL
 */
@Service
class TalentPoolService(
    private val redisTemplate: RedisTemplate<String, String>,
    private val objectMapper: ObjectMapper
) {

    companion object {
        private const val TALENT_TIMEOUT_MINUTES = 15L// 15분 active with TTL
        private const val TALENT_KEY_PREFIX = "talent"
        private const val USER_KEY_PREFIX = "user"
    }
    // ============================================================================
    // Finder 사용
    // ============================================================================

    fun getRegisteringMaps(userId: Long): RegisteringPoolResponse {
        val ttlOfUser = getTTLOfUser(userId)
        val mapList = redisTemplate.opsForSet()
            .members(getRegisteringMapKey(userId))
            ?.map { it.toLong() }
            ?.toList()
            ?: emptyList()

        return RegisteringPoolResponse(mapList, ttlOfUser)
    }

    private fun getTTLOfUser(userId: Long): Long {
        val key = getTalentUserKey(userId)
        return redisTemplate.getExpire(key)
    }


    /**
     * 유저의 캐릭터를 map Id의 맵의 인재풀에 등록하는 redis 메소드
     *
     * 1. talent:{mapId}:users 의 Set에 추가 -> TTL 없음
     * 2. talent:user:{userId} 에 유저가 등록한 캐릭터 정보 추가 -> TTL 15
     * 3. user:{userId}:talent:maps 에 유저가 등록한 맵들을 저장 : 언제 없애나? 없애지 않는다 2번 ttl 만료시 없어지는 것
     */
    fun registerToTalentPool(mapId: Long, characterId: String): TalentDto {
        val userId = UserService.getLoginUserId()
        val talentRequest = getTalentDtoFrom(userId, characterId)
        // Store in Redis with TTL - manual JSON serialization
        val characterKey = getTalentUserKey(userId)
        val json = objectMapper.writeValueAsString(talentRequest)

        redisTemplate.opsForValue().set(
            characterKey,
            json,
            TALENT_TIMEOUT_MINUTES,
            TimeUnit.MINUTES
        )

        // Map Set 에는 TTL을 설정하지 않음
        // todo: Schedule 에서 비동기로 삭제하는 로직 추가
        val mapKey = getTalentMapKey(mapId)
        redisTemplate.opsForSet().add(mapKey, userId.toString())
        // 등록 유저가 조회하는 곳에도 저장
        addUserRegisterMap(userId, mapId)

        return talentRequest
    }

    private fun addUserRegisterMap(userId: Long, mapId: Long) {
        val key = getRegisteringMapKey(userId)
        redisTemplate.opsForSet().add(key, mapId.toString())
    }

    /**
     * 특정 맵의 인재풀에서 해제
     */
    fun removeFromTalentPool(mapId: Long) {
        val userId = UserService.getLoginUserId()
        // Remove from map set
        val mapKey = getTalentMapKey(mapId)
        redisTemplate.opsForSet().remove(mapKey, userId.toString())
        removeRegisterMap(userId, mapId)
    }

    private fun removeRegisterMap(userId: Long, mapId: Long) {
        val key = getRegisteringMapKey(userId)
        redisTemplate.opsForSet().remove(key, mapId.toString())
    }

    /**
     * 사용자의 활성 상태를 업데이트 하는 redis call
     * 비활성화 상태여도 누르면 다시 등록한다.
     */
    fun renewHeartbeat(characterId: String): Boolean {
        val userId = UserService.getLoginUserId()
        val key = getTalentUserKey(userId)

        // Check if key exists
        val json = redisTemplate.opsForValue().get(key)
            ?: objectMapper.writeValueAsString(getTalentDtoFrom(userId, characterId))

        // Renew TTL by re-setting the value
        redisTemplate.opsForValue().set(
            key,
            json,
            TALENT_TIMEOUT_MINUTES,
            TimeUnit.MINUTES
        )

        return true
    }


    // ============================================================================
    // Leader 사용
    // ============================================================================
    private fun getTalentDtoFrom(userId: Long, characterId: String): TalentDto {
        return transaction { // if not exist make new request
            CharacterEntity.findByUserAndCharacterId(userId, characterId)
                ?.let(TalentDto::fromEntity)
                ?: throw PartyBadRequest(ErrorCode.CHARACTER_NOT_FOUND, "Character Not Found")
        }
    }

    /**
     * Get all active talents for a map
     *
     * @param mapId Map ID
     * @return List of active talent character (one of user)
     */
    fun getTalentPool(mapId: Long): List<TalentDto> {
        val mapKey = getTalentMapKey(mapId)
        // Get all user IDs registered for this map
        val userIds = redisTemplate.opsForSet().members(mapKey) ?: emptySet()

        // For each user ID, check if they have an active talent key and fetch their TalentDto
        return userIds.mapNotNull { userIdStr ->
            val userId = userIdStr.toLongOrNull() ?: return@mapNotNull null
            val userKey = getTalentUserKey(userId)

            // Get the JSON value from Redis - if key doesn't exist or is expired, this returns null
            val json = redisTemplate.opsForValue().get(userKey) ?: return@mapNotNull null

            // Deserialize JSON to TalentDto
            try {
                objectMapper.readValue(json, TalentDto::class.java)
            } catch (e: Exception) {
                null // Skip invalid JSON
            }
        }
    }

    // ============================================================================
    // Util Func
    // ============================================================================


    /**
     * 유저의 캐릭터 정보와 TTL이 설정되는 키
     */
    private fun getTalentUserKey(userId: Long): String {
        return "$TALENT_KEY_PREFIX:user:$userId"
    }

    /**
     * 맵 별로 등록한 유저가 들어가 있는 키
     */
    private fun getTalentMapKey(mapId: Long): String {
        return "$TALENT_KEY_PREFIX:$mapId:users"
    }

    /**
     * 유저가 인재풀 등록중인 맵을 가져오기 위한 키
     */
    private fun getRegisteringMapKey(userId: Long): String {
        return "$USER_KEY_PREFIX:$userId:talent:maps"
    }
}

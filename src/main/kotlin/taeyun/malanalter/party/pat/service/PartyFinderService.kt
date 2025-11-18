package taeyun.malanalter.party.pat.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Service
import taeyun.malanalter.config.exception.ErrorCode
import taeyun.malanalter.config.exception.PartyBadRequest
import taeyun.malanalter.config.exception.PartyServerError
import taeyun.malanalter.party.character.CharacterTable
import taeyun.malanalter.party.pat.dao.ApplicantTable
import taeyun.malanalter.party.pat.dao.PartyStatus
import taeyun.malanalter.party.pat.dao.PartyTable
import taeyun.malanalter.party.pat.dao.PositionTable
import taeyun.malanalter.party.pat.dto.*
import taeyun.malanalter.user.UserService
import java.util.UUID.randomUUID

val logger = KotlinLogging.logger {}

@Service
class PartyFinderService(val talentPoolService: TalentPoolService, private val partyRedisService: PartyRedisService) {

    fun registerToTalentPool(mapId: Long, characterId: String): Long {
        try {

            talentPoolService.registerToTalentPool(mapId, characterId)

            transaction {
                val userId = UserService.getLoginUserId()
                val characterRow = CharacterTable.selectAll()
                    .where { CharacterTable.userId eq userId and (CharacterTable.id eq characterId) }
                    .singleOrNull()
                    ?: throw PartyBadRequest(
                        ErrorCode.CHARACTER_NOT_FOUND, ErrorCode.CHARACTER_NOT_FOUND.defaultMessage
                    )

                val publishData = TalentResponse(
                    userId = userId.toString(),
                    characterId = characterId,
                    lastSent = null,
                    name = characterRow[CharacterTable.name],
                    level = characterRow[CharacterTable.level],
                    job = characterRow[CharacterTable.job],
                    comment = characterRow[CharacterTable.comment]
                )
                partyRedisService.publishMessage(PartyRedisService.talentRegisterTopic(mapId), publishData)
            }
            return mapId
        } catch (ex: Exception) {
            val randomUUID = randomUUID()
            logger.error { "$randomUUID Error in inviting user to server ${ex.message} ${ex.javaClass}" }
            throw PartyServerError(
                uuid = randomUUID.toString(),
                message = "Error in inviting user to server",
                rootCause = ex
            )
        }

    }

    fun deleteTalentMap(mapId: Long) {
        val userId = UserService.getLoginUserId()
        talentPoolService.removeFromTalentPool(mapId)
        val redisMessage = hashMapOf<String, String>()
        redisMessage.put("userId", userId.toString())
        partyRedisService.publishMessage(PartyRedisService.talentUnRegisterTopic(mapId), redisMessage)
    }

    fun renewFinderHeartbeat(characterId: String) {
        talentPoolService.renewHeartbeat(characterId)
    }

    fun getRegisteringPool(): RegisteringPoolResponse {
        val userId = UserService.getLoginUserId()
        return talentPoolService.getRegisteringMaps(userId)
    }

    fun getPartiesByMaps(mapIds: List<Long>): List<PartyResponse> {
        return transaction {
            PartyTable.selectAll()
                .where { PartyTable.mapId inList mapIds and (PartyTable.status eq PartyStatus.RECRUITING) }
                .filter { partyRedisService.getPartyTTL(it[PartyTable.id].value) > 0 }
                .map {
                    val positions = PositionTable.selectAll()
                        .where { PositionTable.partyId eq it[PartyTable.id].value }
                        .orderBy(PositionTable.orderNumber)
                        .map(PositionDto::from)
                    PartyResponse.withPositions(it, positions)
                }
        }

    }

    fun getMapDiscordMessages(mapIds: List<Long>): Map<Long, List<DiscordMessageDto>> {
        return partyRedisService.getDiscordOfMaps(mapIds)
    }

    fun applyParty(partyApplyRequest: PartyApplyRequest) {
        transaction {
            val applyUserId = UserService.getLoginUserId()

            try {
                val newApplyId = ApplicantTable.insertAndGetId {
                    it[ApplicantTable.partyId] = partyApplyRequest.partyId
                    it[ApplicantTable.positionId] = partyApplyRequest.positionId
                    it[ApplicantTable.characterId] = partyApplyRequest.characterId
                    it[ApplicantTable.applyUserId] = applyUserId
                }
                val characterRow = (CharacterTable.selectAll()
                    .where { CharacterTable.id eq partyApplyRequest.characterId }.singleOrNull()
                    ?: throw PartyBadRequest(
                        ErrorCode.CHARACTER_NOT_FOUND,
                        ErrorCode.CHARACTER_NOT_FOUND.defaultMessage
                    ))
                val redisMessage = ApplicantRes(
                    applyId = newApplyId.value.toString(),
                    applyUserId = applyUserId.toString(),
                    characterId = partyApplyRequest.characterId,
                    name= characterRow[CharacterTable.name],
                    level = characterRow[CharacterTable.level],
                    job = characterRow[CharacterTable.job],
                    comment = characterRow[CharacterTable.comment],
                    positionId = partyApplyRequest.positionId,
                )
                partyRedisService.publishMessage(PartyRedisService.partyApplyTopic(partyApplyRequest.partyId), redisMessage)

            } catch (ex: ExposedSQLException) {
                when {
                    ex.message?.contains("unique constraint") == true -> throw PartyBadRequest(
                        ErrorCode.ALREADY_APPLIED,
                        "이미 지원한 파티입니다."
                    )

                    ex.message?.contains("foreign key constraint") == true -> throw PartyBadRequest(
                        ErrorCode.INVALID_PARTY_APPLIED,
                        "삭제된 파티이거나 이미 구인된 포지션입니다."
                    )

                    else -> throw ex
                }
            }
        }

    }


}
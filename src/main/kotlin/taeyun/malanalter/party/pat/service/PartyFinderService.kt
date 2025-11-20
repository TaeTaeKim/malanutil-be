package taeyun.malanalter.party.pat.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.StdOutSqlLogger
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.postgresql.util.PSQLException
import org.springframework.stereotype.Service
import taeyun.malanalter.auth.discord.DiscordService
import taeyun.malanalter.config.exception.BaseException
import taeyun.malanalter.config.exception.ErrorCode
import taeyun.malanalter.config.exception.PartyBadRequest
import taeyun.malanalter.config.exception.PartyServerError
import taeyun.malanalter.party.character.CharacterEntity
import taeyun.malanalter.party.character.CharacterTable
import taeyun.malanalter.party.pat.dao.*
import taeyun.malanalter.party.pat.dto.*
import taeyun.malanalter.party.pat.service.PartyRedisService.Companion.partyApplyTopic
import taeyun.malanalter.party.pat.service.PartyRedisService.Companion.partyUpdateTopic
import taeyun.malanalter.user.UserService
import java.util.*
import java.util.UUID.randomUUID

val logger = KotlinLogging.logger {}

@Service
class PartyFinderService(
    val talentPoolService: TalentPoolService,
    private val partyRedisService: PartyRedisService,
    val discordService: DiscordService
) {

    fun registerToTalentPool(mapId: Long, characterId: String): Long {
        try {

            talentPoolService.registerToTalentPool(mapId, characterId)

            val publishData = transaction {
                val userId = UserService.getLoginUserId()
                val characterRow = CharacterTable.selectAll()
                    .where { CharacterTable.userId eq userId and (CharacterTable.id eq characterId) }
                    .singleOrNull()
                    ?: throw PartyBadRequest(
                        ErrorCode.CHARACTER_NOT_FOUND, ErrorCode.CHARACTER_NOT_FOUND.defaultMessage
                    )

                TalentResponse(
                    userId = userId.toString(),
                    characterId = characterId,
                    isSent = false,
                    name = characterRow[CharacterTable.name],
                    level = characterRow[CharacterTable.level],
                    job = characterRow[CharacterTable.job],
                    comment = characterRow[CharacterTable.comment]
                )
            }
            partyRedisService.publishMessage(PartyRedisService.talentRegisterTopic(mapId), publishData)
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
        // 제거하려는 맵의 초대장을 모두 제거
        transaction {
            addLogger(StdOutSqlLogger)
            val join = Invitation.join(PartyTable, JoinType.LEFT)
            join.delete(Invitation) { PartyTable.mapId eq mapId }
        }
        talentPoolService.removeFromTalentPool(mapId)
        val redisMessage = hashMapOf<String, String>()
        redisMessage.put("userId", userId.toString())
        partyRedisService.publishMessage(PartyRedisService.talentUnRegisterTopic(mapId), redisMessage)
    }

    // 사용자의 인재풀 갱신
    fun renewFinderHeartbeat(characterId: String) {
        val userId = UserService.getLoginUserId()
        // ttl이 아직 끝나지 않았으면 갱신만
        if (talentPoolService.getTTLOfUser(userId) >= 3) {
            talentPoolService.renewHeartbeat(characterId)
        } else {
            val characterEntity = transaction {
                CharacterEntity.findById(characterId)
                    ?: throw PartyBadRequest(
                        ErrorCode.CHARACTER_NOT_FOUND, ErrorCode.CHARACTER_NOT_FOUND.defaultMessage
                    )
            }

            val registeringMaps = talentPoolService.getRegisteringMaps(userId)
            val publishData = TalentResponse(
                userId = userId.toString(),
                characterId = characterId,
                isSent = false,
                name = characterEntity.name,
                level = characterEntity.level,
                job = characterEntity.job,
                comment = characterEntity.comment
            )
            registeringMaps.mapIds.forEach { mapId ->
                talentPoolService.registerToTalentPool(mapId, characterId)
                partyRedisService.publishMessage(PartyRedisService.talentRegisterTopic(mapId), publishData)
            }
        }
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

    private fun isUserInParty(userId: Long): Boolean {
        return transaction {
            PositionTable.select(PositionTable.id)
                .where {
                    PositionTable.assignedUserId eq userId
                }.singleOrNull() != null
        }
    }

    fun applyParty(partyApplyRequest: PartyApplyRequest) {
        val applicantRes = try {
            transaction {
                val applyUserId = UserService.getLoginUserId()
                // 파티에 참여중이면 지원 불가
                if (isUserInParty(applyUserId)) {
                    throw PartyBadRequest(
                        ErrorCode.USER_ALREADY_IN_PARTY,
                        "이미 파티에 참여중인 유저는 지원할 수 없습니다."
                    )
                }
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
                ApplicantRes(
                    actionType = ApplicantAction.ADD,
                    applyId = newApplyId.value.toString(),
                    applyUserId = applyUserId.toString(),
                    characterId = partyApplyRequest.characterId,
                    name = characterRow[CharacterTable.name],
                    level = characterRow[CharacterTable.level],
                    job = characterRow[CharacterTable.job],
                    comment = characterRow[CharacterTable.comment],
                    positionId = partyApplyRequest.positionId,
                )
            }
        } catch (ex: ExposedSQLException) {
            val cause = ex.cause
            if (cause is PSQLException) {
                when (cause.sqlState) {
                    "23503" -> throw PartyBadRequest( // foreign key 위반
                        ErrorCode.INVALID_PARTY_APPLIED,
                        "삭제된 파티이거나 이미 구인된 포지션입니다."
                    )

                    "23505" -> throw PartyBadRequest( // unique 제약조건 위반 -> 이미 신청한 파티
                        ErrorCode.ALREADY_APPLIED,
                        "이미 지원한 파티입니다."
                    )

                    else -> {
                        throw ex
                    }
                }
            } else throw ex
        }
        // 지원 완료 후 처리 (예: 알림 전송 등)는 트랜잭션 외부에서 수행
        try {
            transaction {
                val partyEntity = PartyEntity.findById(partyApplyRequest.partyId)
                    ?: throw PartyBadRequest(
                        ErrorCode.PARTY_NOT_FOUND,
                        "지원한 파티를 찾을 수 없습니다."
                    )
                if (partyEntity.discordNotification) {
                    discordService.sendDirectMessage(
                        partyEntity.leaderId.value,
                        partyApplyDiscordMessage(applicantRes, partyApplyRequest.positionName)
                    )
                }
            }
        } catch (ex: Exception) {
            val uuid = randomUUID().toString()
            logger.error { "[$uuid] Error sending Apply Discord notification: ${ex.message}" }
        }
        // 웹소켓으로 실시간 전송
        partyRedisService.publishMessage(
            PartyRedisService.partyApplyTopic(partyApplyRequest.partyId),
            applicantRes
        )

    }

    private fun partyApplyDiscordMessage(res: ApplicantRes, positionName: String): String {
        return "새로운 파티 지원이 도착했습니다! \n" +
                "지원 포지션 : $positionName\n" +
                "지원 캐릭터 정보: LV:${res.level} ${res.job} 💬${res.comment}\n"

    }

    fun getAppliedPositions(): List<AppliedPositionDto> {
        val applyUserId = UserService.getLoginUserId()
        return transaction {
            ApplicantTable.selectAll()
                .where { ApplicantTable.applyUserId eq applyUserId }
                .map {
                    AppliedPositionDto(
                        partyId = it[ApplicantTable.partyId].value,
                        positionId = it[ApplicantTable.positionId].value
                    )
                }
        }
    }

    fun cancelApplication(partyId: String, positionId: String) {
        val applyUserId = UserService.getLoginUserId()
        transaction {
            val deleteCount = ApplicantTable.deleteWhere {
                (ApplicantTable.partyId eq partyId) and
                        (ApplicantTable.positionId eq positionId) and
                        (ApplicantTable.applyUserId eq applyUserId)
            }

            if (deleteCount == 0) {
                throw PartyBadRequest(
                    ErrorCode.APPLICANT_NOT_FOUND,
                    "해당 파티 지원 내역을 찾을 수 없습니다."
                )
            }
        }

        val result = ApplicantRes.makeCancelRes(applyUserId.toString(), positionId)
        partyRedisService.publishMessage(PartyRedisService.partyApplyTopic(partyId), result)
    }

    fun getInvitations(): List<InvitationDto> {
        val userId = UserService.getLoginUserId()
        return transaction {
            // 유저의 모든 초대 조회 Map<positionId, invitationId>
            (Invitation leftJoin PositionTable)
                .join(PartyTable, JoinType.LEFT, onColumn = Invitation.partyId, otherColumn = PartyTable.id)
                .selectAll()
                .where { Invitation.invitedUserId eq userId and (Invitation.status eq InvitationStatus.PENDING)  }
                .map { InvitationDto.from(it) }
        }
    }

    fun rejectInvitation(invitationId: String) {
        val userId = UserService.getLoginUserId()
        transaction {
            val deleteCount = Invitation
                .update(where = { Invitation.id eq UUID.fromString(invitationId) and (Invitation.invitedUserId eq userId) }) {
                    it[Invitation.status] = InvitationStatus.REJECTED
                }
            if (deleteCount == 0) {
                throw PartyBadRequest(ErrorCode.INVITATION_NOT_FOUND, "초대장을 찾을 수 없습니다.")
            }
        }
    }

    // 파티 초대 수락
    // 포지션을 락과 함께 조회 후 유저 할당
    // Postiion 상태변경 메세지 전달.
    fun acceptInvitation(invitationId: String, characterId: String) {
        val userId = UserService.getLoginUserId()
        try {

            transaction {
                addLogger(StdOutSqlLogger)
                val (invitedPositionId, mapId) = (Invitation leftJoin PartyTable).select(
                    Invitation.positionId,
                    PartyTable.mapId
                ).where { Invitation.invitedUserId eq userId and (Invitation.id eq UUID.fromString(invitationId)) }
                    .singleOrNull()
                    ?.let { Pair(it[Invitation.positionId], it[PartyTable.mapId]) }
                    ?: throw PartyBadRequest(ErrorCode.INVITATION_NOT_FOUND, "초대장을 찾을 수 없습니다.")
                val characterEntity = CharacterEntity.findByUserAndCharacterId(userId, characterId)
                    ?: throw PartyBadRequest(ErrorCode.CHARACTER_NOT_FOUND,"할당할 캐릭터를 찾을 수 없습니다.")

                val positionRow = (PositionTable.selectAll()
                    .where { PositionTable.id eq invitedPositionId }
                    .forUpdate()
                    .singleOrNull()
                    ?: throw PartyBadRequest(ErrorCode.POSITION_NOT_FOUND, "초대된 포지션을 찾을 수 없습니다."))

                if (positionRow[PositionTable.status] == PositionStatus.COMPLETED) {
                    throw PartyBadRequest(ErrorCode.POSITION_ALREADY_OCCUPIED, "이미 할당된 포지션입니다.")
                }

                // 포지션에 유저 할당
                val updatedPositionDto = PositionTable.updateReturning(where = { PositionTable.id eq invitedPositionId }) {
                    it[assignedUserId] = userId
                    it[assignedCharacterId] = characterEntity.id
                    it[assignedCharacterName] = characterEntity.name
                    it[status] = PositionStatus.COMPLETED
                    it[description] = "${characterEntity.level} ${characterEntity.job}"
                }
                    .single()
                    .let { PositionDto.from(it) }
                // redis 로 포지션 업데이트 메세지 전송
                partyRedisService.publishMessage(partyUpdateTopic(mapId), updatedPositionDto)
                // 파티장에게도 파티 업데이트 메세지 전송 party:apply:{partyId} actionType : ACCEPT
                partyRedisService.publishMessage(
                    partyApplyTopic(positionRow[PositionTable.partyId].value),
                    ApplicantRes.makeAcceptRes(userId, invitedPositionId.value, characterEntity)
                )
                Invitation.deleteWhere { Invitation.id eq UUID.fromString(invitationId) }
            }
            // 모든 구인 중 및 인재풀에서 제거 - 등록중 맵에서는 제거하지 않는다 -> 추방 시 다시 구인 중 등록하도록
            talentPoolService.removeFromAllTalentPool()
        }catch (ex: BaseException){
            // 이미 구인 포지션에 대한 초대일 경우 Invitation을 invalid 로 변경
            if(ex.errorCode == ErrorCode.POSITION_ALREADY_OCCUPIED){
                transaction { InvitationEntity.changeStatus(invitationId, InvitationStatus.INVALID) }
            }
            throw ex
        }

    }
}
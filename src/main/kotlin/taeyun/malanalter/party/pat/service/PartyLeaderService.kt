package taeyun.malanalter.party.pat.service

import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.postgresql.util.PSQLException
import org.springframework.stereotype.Service
import taeyun.malanalter.config.exception.AlerterBadRequest
import taeyun.malanalter.config.exception.ErrorCode.*
import taeyun.malanalter.config.exception.PartyBadRequest
import taeyun.malanalter.party.character.CharacterEntity
import taeyun.malanalter.party.character.CharacterTable
import taeyun.malanalter.party.pat.dao.*
import taeyun.malanalter.party.pat.dto.*
import taeyun.malanalter.party.pat.service.PartyRedisService.Companion.partyCreateTopic
import taeyun.malanalter.party.pat.service.PartyRedisService.Companion.partyDeleteTopic
import taeyun.malanalter.party.pat.service.PartyRedisService.Companion.partyUpdateTopic
import taeyun.malanalter.user.UserService
import java.util.*

@Service
class PartyLeaderService(
    val talentPoolService: TalentPoolService,
    val partyRedisService: PartyRedisService,
) {
    /**
     * 로그인 유저가 리더인 파티 조회
     * @return 리더인 파티 정보, 없으면 null
     */
    fun getLeaderParty(): PartyResponse? = transaction {
        val userId = UserService.getLoginUserId()

        // Find party where user is the leader
        PartyTable.selectAll()
            .where { PartyTable.leaderId eq userId }
            .singleOrNull()
            ?.let { partyRow ->
                // Fetch positions for this party
                val partyId = partyRow[PartyTable.id].value
                val positions = PositionTable.selectAll()
                    .where { PositionTable.partyId eq partyId }
                    .orderBy(PositionTable.orderNumber)
                    .map { PositionDto.from(it) }

                // Return party with positions
                PartyResponse.withPositions(partyRow, positions)
            }
    }

    /**
     * 파티 생성 서비스
     * @param mapId 생성할 파티의 맵 ID
     * @param partyCreate 파티 생성 요청 DTO
     * @return 생성된 파티 응답 DTO
     * Party, Position Id 는 UUID로 생성
     *
     */
    fun createParty(mapId: Long, partyCreate: PartyCreate): PartyResponse {
        val userId = UserService.getLoginUserId()

        val createdParty = transaction {
            // Validate: Leader's character exists
            CharacterTable.selectAll()
                .where { CharacterTable.userId eq userId and (CharacterTable.id eq partyCreate.characterId) }
                .singleOrNull() ?: throw AlerterBadRequest(
                BAD_REQUEST,
                "파티장의 캐릭터를 찾지 못했습니다."
            )
            if (partyCreate.hasPositions) {
                if (partyCreate.positions.isEmpty()) {
                    throw AlerterBadRequest(
                        BAD_REQUEST,
                        "포지션 파티는 최소 1개 이상의 포지션이 필요합니다."
                    )
                }
                if (partyCreate.positions.none { it.isLeader }) {
                    throw AlerterBadRequest(
                        BAD_REQUEST,
                        "파티장 포지션이 필요합니다."
                    )
                }
            }

            // Generate party ID
            val partyId = UUID.randomUUID().toString()

            // Insert party into database
            PartyTable.insert {
                it[id] = partyId
                it[PartyTable.mapId] = mapId
                it[hasPosition] = partyCreate.hasPositions
                it[description] = partyCreate.description
                it[numPeople] = partyCreate.numPeople
                it[channel] = partyCreate.channel
                it[leaderId] = userId
                it[leaderCharacter] = partyCreate.characterId
                it[discordNotification] = partyCreate.discordNotification
                // status, lastHeartbeatAt, createdAt, updatedAt have default values
            }

            savePartyPositions(partyCreate, partyId)

            // Save party creation history
            PartyHistory.insert {
                it[PartyHistory.userId] = userId
                it[PartyHistory.partyData] = partyCreate
                it[PartyHistory.mapId] = mapId
            }

            // 파티 heartbeat 시작
            partyRedisService.registerPartyHeartbeat(partyId)

            // Fetch created party to return
            val createdParty = PartyTable.selectAll()
                .where { PartyTable.id eq partyId }
                .single()

            val positionDtos = PositionTable.selectAll()
                .where { PositionTable.partyId eq partyId }
                .map(PositionDto::from)

            // Return party response with positions
            PartyResponse.withPositions(createdParty, positionDtos)

        }
        partyRedisService.publishMessage(partyCreateTopic(mapId), createdParty)
        return createdParty
    }

    private fun savePartyPositions(partyRequest: PartyCreate, partyId: String) {
        val userId = UserService.getLoginUserId()
        val leaderCharacter = CharacterEntity.findById(partyRequest.characterId)
            ?: throw PartyBadRequest(CHARACTER_NOT_FOUND)
        for (idx in 0 until partyRequest.numPeople) {
            val positionId = UUID.randomUUID().toString()
            if (partyRequest.hasPositions) {
                val position = partyRequest.positions[idx]
                PositionTable.insert {
                    it[id] = positionId
                    it[PositionTable.partyId] = partyId
                    it[name] = position.name
                    it[description] = position.description
                    it[price] = position.price
                    it[isLeader] = position.isLeader
                    it[status] = position.status
                    it[isPriestSlot] = position.isPriestSlot
                    it[preferJob] = position.preferJob.joinToString(",")
                    it[orderNumber] = idx

                    // If this is the leader position, auto-assign to creator
                    if (position.isLeader) {
                        it[assignedUserId] = userId
                        it[assignedCharacterId] = partyRequest.characterId
                        it[assignedCharacterName] = leaderCharacter.name
                    }
                }
            } else {
                val leaderPosition = idx == 0
                PositionTable.insert {
                    it[id] = positionId
                    it[PositionTable.partyId] = partyId
                    it[name] = if (leaderPosition) "파장" else "${idx + 1}"
                    it[description] =
                        if (leaderPosition) "${leaderCharacter.level}${leaderCharacter.job}" else null
                    it[isLeader] = leaderPosition // 첫번쨰 포지션을 강제로 리더로 지정
                    it[status] = if (leaderPosition) PositionStatus.COMPLETED else PositionStatus.RECRUITING
                    it[isPriestSlot] = false
                    // If this is the leader position, auto-assign to creator
                    if (leaderPosition) {
                        it[assignedUserId] = userId
                        it[assignedCharacterId] = partyRequest.characterId
                        it[assignedCharacterName] = leaderCharacter.name
                    }
                }
            }

        }
    }

    // todo: 파티 없애기 시 들어와 있던 지원자들을 free 시켜줘야 함
    fun deleteParty(partyId: String) {
        val userId = UserService.getLoginUserId()
        val mapCode = transaction {
            // Verify that the party exists and the user is the leader
            val resultRow = PartyTable.select(PartyTable.mapId)
                .where { PartyTable.id eq partyId and (PartyTable.leaderId eq userId) }
                .singleOrNull() ?: throw AlerterBadRequest(
                BAD_REQUEST,
                "삭제할 파티를 찾지 못했습니다."
            )
            partyRedisService.removePartyHeartbeat(partyId)
            // Delete the party (positions will be cascade deleted)
            PartyTable.deleteWhere { PartyTable.id eq partyId }
            resultRow[PartyTable.mapId]
        }
        partyRedisService.publishMessage(partyDeleteTopic(mapCode), hashMapOf("partyId" to partyId))
    }

    /**
     * 파티 자리를 파티장이 직접 수정할 경우 != 초대요청 수락 혹은 참가요청 수락
     * 파티 포지션 자리는 무조건 비관적 락으로 실행한다. -> 경쟁적으로 자리 요청이 생길 수 있다. Conflict 상황이 많다.
     * 트랜잭션 락 시간이 길지 않다.
     * 리 트라이가 필요없다.
     *
     * party:{mapId}:update 를 줘야한다.
     * COMPLETED -> RECRUITING 요청은 없다.
     */
    fun updatePartyPosition(update: PositionUpdateReq, partyId: String, positionId: String) {
        // 요청 검증
        if (update.status == PositionStatus.COMPLETED && (update.recruitedJob == null || update.recruitedLevel == null)) {
            throw PartyBadRequest(POSITION_INPUT_INVALID, POSITION_INPUT_INVALID.defaultMessage)
        }
        // Transaction 은 업데이트 후 바로 release 하여 다른 락킹 트랜잭션을 풀어준다.
        val positionDto = transaction {
            val row = PositionTable.selectAll()
                .where { PositionTable.partyId eq partyId and (PositionTable.id eq positionId) }
                .forUpdate()
                .singleOrNull() ?: throw PartyBadRequest(POSITION_NOT_FOUND, "포지션을 찾을 수 없습니다.")

            // 사용자가 변경하려 했지만 중간에 초대 요청을 수락한 사람이 있을 경우 막아야한다.
            if (row[PositionTable.status] == PositionStatus.COMPLETED && update.status == PositionStatus.COMPLETED) {
                throw PartyBadRequest(POSITION_ALREADY_OCCUPIED, POSITION_ALREADY_OCCUPIED.defaultMessage)
            }

            PositionTable.updateReturning(where = { PositionTable.id eq positionId }) {
                it[name] = update.name
                it[price] = update.price
                it[preferJob] = update.preferJob?.joinToString(",")
                it[status] = update.status
                if (update.status == PositionStatus.COMPLETED) {
                    it[description] = "${update.recruitedLevel} ${update.recruitedJob}"
                } else {
                    it[description] = null
                    it[assignedUserId] = null
                    it[assignedCharacterId] = null
                    it[assignedCharacterName] = null
                }
            }.single().let { PositionDto.from(it) }
            // redis publish
        }
        partyRedisService.publishMessage(partyUpdateTopic(update.mapCode), positionDto)


    }

    fun getPartyCreationHistory(mapId: Long): PartyCreate? {
        val userId = UserService.getLoginUserId()
        return transaction {
            PartyHistory.selectAll()
                .where { PartyHistory.userId eq userId and (PartyHistory.mapId eq mapId) }
                .orderBy(PartyHistory.createdAt to SortOrder.DESC)
                .limit(1)
                .map { it[PartyHistory.partyData] }
                .firstOrNull()
        }
    }

    /**
     * 유저에게 파티 초대 메세지 발송
     * 5분 이내에 동일 유저에게 초대 메세지 발송 불가
     */
    // todo : redis 로 해당 유저 에게 초대 메세지 publish
    fun inviteUserToParty(inviteUserId: Long) {
        val leaderId = UserService.getLoginUserId()

        transaction {
            // 리더의 파티 존재 확인
            val party =
                PartyEntity.findByLeaderId(leaderId) ?: throw PartyBadRequest(PARTY_NOT_FOUND, "현재 리더인 파티가 존재하지 않습니다.")
            when (party.status) {
                PartyStatus.FULLED -> throw PartyBadRequest(PARTY_FULL, "파티가 가득 찼습니다.")
                PartyStatus.INACTIVE -> throw PartyBadRequest(PARTY_INACTIVE, "비활성화된 파티입니다.")
                else -> {}
            }
            val userTalentPool = talentPoolService.getRegisteringMaps(inviteUserId)
            if (party.mapId !in userTalentPool.mapIds) {
                throw PartyBadRequest(CHARACTER_NOT_IN_TALENT, "유저가 해당 맵의 탤런트에 없습니다")
            }

            // 중복 초대 확인
            if (InvitationEntity.alreadyInvited(party.id.value, inviteUserId)) {
                throw PartyBadRequest(PARTY_ALREADY_INVITED, "해당 유저에게 이미 초대 메세지를 보낸 상태입니다.")
            }

            // 초대발송 db저장 및 redis publish
            InvitationEntity.new {
                this.partyId = party.id
                this.invitedUserId = inviteUserId
            }
        }
    }

    fun getTalentPool(mapId: Long, partyId: String): List<TalentResponse> {
        val talentUserList = talentPoolService.getTalentPool(mapId)
        val invitedTimeByPartyId = transaction {
            InvitationEntity.getInvitedTimeByPartyId(partyId)
        }
        return talentUserList.map { TalentResponse.from(it, invitedTimeByPartyId[it.userId]) }
    }

    fun getPartyHeartbeat(partyId: String): Long {
        return partyRedisService.getPartyTTL(partyId)
    }

    fun renewPartyHeartbeat(partyId: String) {
        partyRedisService.registerPartyHeartbeat(partyId)
    }

    fun getApplicant(): List<ApplicantRes> {
        val leaderUserId = UserService.getLoginUserId()
        // 파티가 있는지 확인
        return transaction {
            val row = (PartyTable.select(PartyTable.id)
                .where { PartyTable.leaderId eq leaderUserId }
                .singleOrNull()
                ?: throw PartyBadRequest(PARTY_NOT_FOUND, PARTY_NOT_FOUND.defaultMessage))
            (ApplicantTable leftJoin CharacterTable).selectAll()
                .where { ApplicantTable.partyId eq row[PartyTable.id] }
                .map {
                    ApplicantRes(
                        actionType = ApplicantAction.NONE,
                        applyId = it[ApplicantTable.id].toString(),
                        applyUserId = it[ApplicantTable.applyUserId].value.toString(),
                        characterId = it[ApplicantTable.characterId].value,
                        name = it[CharacterTable.name],
                        level = it[CharacterTable.level],
                        job = it[CharacterTable.job],
                        comment = it[CharacterTable.comment],
                        positionId = it[ApplicantTable.positionId].value
                    )

                }
        }
    }

    // 요청을 수락하는 API로 Position 에 넣어야한다 -> Position 에 락을 동반해야한다.
    // 이미 유저가 다른 파티에 들어갔는지도 체크해야한다. -> DB level에서 유니크 인덱스 설정으로 동작하게 한다.
    fun acceptApplicant(acceptReq: ApplyAcceptReq): PositionDto {
        try {
            val result = transaction {
                // Applicant 가 있는지 검사
                ApplicantEntity.findById(UUID.fromString(acceptReq.applyId))
                    ?: throw PartyBadRequest(USER_ALREADY_IN_PARTY)
                // 신청자의 캐릭터 조회
                val characterEntity = (CharacterEntity.findById(acceptReq.applicantCharacterId)
                    ?: throw PartyBadRequest(CHARACTER_NOT_FOUND, "신청자의 캐릭터를 찾을 수 없습니다."))
                // 1. Lock Position for update
                val positionRow = PositionTable.selectAll()
                    .where { PositionTable.partyId eq acceptReq.partyId and (PositionTable.id eq acceptReq.positionId) }
                    .forUpdate()
                    .singleOrNull()
                    ?: throw PartyBadRequest(POSITION_NOT_FOUND)

                // 2. 포지션의 자리 마감 확인
                if (positionRow[PositionTable.status] == PositionStatus.COMPLETED) {
                    throw PartyBadRequest(POSITION_ALREADY_OCCUPIED)
                }

                // 3. 업데이트(Unique Key) : 유니크 조건에 의해 이미 들어가 파티간 있는 상황이라면 Unique Violation 발생
                val updatedPosition =
                    PositionTable.updateReturning(where = { PositionTable.id eq acceptReq.positionId }) {
                        it[status] = PositionStatus.COMPLETED
                        it[assignedUserId] = acceptReq.applicantUserId.toLong()
                        it[assignedCharacterId] = characterEntity.id.value
                        it[assignedCharacterName] = characterEntity.name
                        it[description] = "${characterEntity.level} ${characterEntity.job}"
                    }.single().let { PositionDto.from(it) }

                // 4. 참가 수락시 지원자는 인재풀, 모든 지원에서 삭제되어야한다.
                // todo: 인재풀에서 제거

                ApplicantTable.deleteWhere { ApplicantTable.applyUserId eq acceptReq.applicantUserId.toLong() }
                // 5. redis publish
                updatedPosition
            }
            partyRedisService.publishMessage(partyUpdateTopic(acceptReq.mapId), result)
            return result
        } catch (e: ExposedSQLException) {
            val cause = e.cause
            // https://adjh54.tistory.com/412 : Postgresql 제약조건 위반 코드
            if (cause is PSQLException && cause.sqlState == "23505") {
                // Constraint violation - user already in another party
                throw PartyBadRequest(
                    USER_ALREADY_IN_PARTY,
                    "사용자가 이미 다른 파티에 참여 중입니다."
                )
            }
            throw e
        }
    }

    // 파티에서 유저를 추방하는 기능
    fun kickOutPartyMember(positionId: String): PositionDto {
        val leaderUserId = UserService.getLoginUserId()
        var mapCode: Long = -1
        val updatedRow = transaction {
            val partyEntity = (PartyEntity.findByLeaderId(leaderUserId)
                ?: throw PartyBadRequest(PARTY_NOT_FOUND, PARTY_NOT_FOUND.defaultMessage))
            // 리더의 파티를 확인

            mapCode = partyEntity.mapId
            PositionTable.updateReturning(where = { PositionTable.partyId eq partyEntity.id and (PositionTable.id eq positionId) }) {
                it[status] = PositionStatus.RECRUITING
                it[assignedUserId] = null
                it[assignedCharacterId] = null
                it[assignedCharacterName] = null
                it[description] = null
            }.singleOrNull() ?: throw PartyBadRequest(POSITION_NOT_FOUND, "해당 포지션을 찾을 수 없습니다.")
        }
        // 추방 정상 처리 후 redis publish
        if (mapCode != -1L) {
            val updatePositionDto = PositionDto.from(updatedRow)
            partyRedisService.publishMessage(partyUpdateTopic(mapCode), updatePositionDto)
            return updatePositionDto
        } else {
            throw PartyBadRequest(BAD_REQUEST, "파티 정보 조회에 실패했습니다.")
        }

    }
}
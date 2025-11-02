package taeyun.malanalter.party.pat

import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Service
import taeyun.malanalter.config.exception.AlerterBadRequest
import taeyun.malanalter.config.exception.ErrorCode.*
import taeyun.malanalter.config.exception.PartyBadRequest
import taeyun.malanalter.party.character.CharacterTable
import taeyun.malanalter.party.pat.dao.*
import taeyun.malanalter.party.pat.dto.*
import taeyun.malanalter.user.UserService
import java.util.*

@Service
class PartyLeaderService(val talentPoolService: TalentPoolService) {

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

        return transaction {
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

            // Insert positions if hasPosition=true
            val positionDtos = if (partyCreate.hasPositions) {
                savePartyPositions(
                    positions = partyCreate.positions,
                    partyId = partyId,
                    userId = userId,
                    characterId = partyCreate.characterId
                )
            } else {
                emptyList()
            }

            // Save party creation history
            PartyHistory.insert {
                it[PartyHistory.userId] = userId
                it[PartyHistory.partyData] = partyCreate
                it[PartyHistory.mapId] = mapId
            }

            // Fetch created party to return
            val createdParty = PartyTable.selectAll()
                .where { PartyTable.id eq partyId }
                .single()

            // Return party response with positions
            PartyResponse.withPositions(createdParty, positionDtos)
        }
    }

    /**
     * 파티 생성 과정에서 포지션을 저장하는 헬퍼함수
     */
    private fun savePartyPositions(
        positions: List<Position>,
        partyId: String,
        userId: Long,
        characterId: String
    ): List<PositionDto> {
        return positions.map { position ->
            val positionId = UUID.randomUUID().toString()

            // Insert position into database
            PositionTable.insert {
                it[id] = positionId
                it[PositionTable.partyId] = partyId
                it[name] = position.name
                it[description] = position.description
                it[isLeader] = position.isLeader
                it[status] = position.status
                it[isPriestSlot] = position.isPriestSlot
                it[preferJob] = position.preferJob.joinToString(",")

                // If this is the leader position, auto-assign to creator
                if (position.isLeader) {
                    it[assignedUserId] = userId
                    it[assignedCharacterId] = characterId
                }
            }

            // Convert to PositionDto using static constructor
            PositionDto.from(
                position = position,
                positionId = positionId,
                partyId = partyId,
                assignedUserId = if (position.isLeader) userId else null,
                assignedCharacterId = if (position.isLeader) characterId else null
            )
        }
    }

    // todo: 파티 없애기 시 들어와 있던 지원자들을 free 시켜줘야 함
    fun deleteParty(partyId: String) {
        val userId = UserService.getLoginUserId()

        transaction {
            // Verify that the party exists and the user is the leader
            PartyTable.selectAll()
                .where { PartyTable.id eq partyId and (PartyTable.leaderId eq userId) }
                .singleOrNull() ?: throw AlerterBadRequest(
                BAD_REQUEST,
                "삭제할 파티를 찾지 못했습니다."
            )

            // Delete the party (positions will be cascade deleted)
            PartyTable.deleteWhere { PartyTable.id eq partyId }
        }
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
            if(party.mapId !in userTalentPool.mapIds ) {
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
}
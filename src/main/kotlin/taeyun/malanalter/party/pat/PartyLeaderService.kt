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
import taeyun.malanalter.config.exception.ErrorCode
import taeyun.malanalter.party.character.CharacterTable
import taeyun.malanalter.party.pat.dao.PartyHistory
import taeyun.malanalter.party.pat.dao.PartyTable
import taeyun.malanalter.party.pat.dao.PositionTable
import taeyun.malanalter.party.pat.dto.PartyCreate
import taeyun.malanalter.party.pat.dto.PartyResponse
import taeyun.malanalter.party.pat.dto.Position
import taeyun.malanalter.party.pat.dto.PositionDto
import taeyun.malanalter.user.UserService
import java.util.*

@Service
class PartyLeaderService {

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
            CharacterTable.selectAll().where{ CharacterTable.userId eq userId and (CharacterTable.id eq partyCreate.characterId) }
                .singleOrNull() ?: throw AlerterBadRequest(
                ErrorCode.BAD_REQUEST,
                "파티장의 캐릭터를 찾지 못했습니다."
            )
            if(partyCreate.hasPositions){
                if(partyCreate.positions.isEmpty()){
                    throw AlerterBadRequest(
                        ErrorCode.BAD_REQUEST,
                        "포지션 파티는 최소 1개 이상의 포지션이 필요합니다."
                    )
                }
                if(partyCreate.positions.none { it.isLeader }){
                    throw AlerterBadRequest(
                        ErrorCode.BAD_REQUEST,
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

    fun deleteParty(partyId: String) {
        val userId = UserService.getLoginUserId()

        transaction {
            // Verify that the party exists and the user is the leader
            PartyTable.selectAll()
                .where { PartyTable.id eq partyId and (PartyTable.leaderId eq userId) }
                .singleOrNull() ?: throw AlerterBadRequest(
                ErrorCode.BAD_REQUEST,
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
}
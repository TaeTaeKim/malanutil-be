package taeyun.malanalter.party.chat

import kotlinx.datetime.toJavaInstant
import org.jetbrains.exposed.v1.jdbc.insertReturning
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Service
import taeyun.malanalter.config.exception.ErrorCode
import taeyun.malanalter.config.exception.PartyBadRequest
import taeyun.malanalter.config.exception.PartyServerError
import taeyun.malanalter.party.character.CharacterEntity
import taeyun.malanalter.party.pat.dao.PositionTable
import taeyun.malanalter.party.pat.service.PartyRedisService
import taeyun.malanalter.user.UserService

@Service
class PartyChatService(
    private val partyRedisService: PartyRedisService
) {
    fun getUserPartyChat(): List<PartyChatDto> {
        val loginUserId = UserService.getLoginUserId()
        return transaction {
            val row = PositionTable.selectAll()
                .where { PositionTable.assignedUserId eq loginUserId }
                .singleOrNull()
                ?: throw PartyBadRequest(ErrorCode.PARTY_NOT_FOUND)

            val foundPartyId = row[PositionTable.partyId].value

            PartyChatTable.selectAll()
                .where { PartyChatTable.partyId eq foundPartyId }
                .orderBy(PartyChatTable.createdAt)
                .map {
                    PartyChatDto(
                        id = it[PartyChatTable.id].value,
                        characterName = it[PartyChatTable.characterName],
                        positionName = it[PartyChatTable.positionName],
                        content = it[PartyChatTable.content],
                        createdAt = it[PartyChatTable.createdAt].toJavaInstant()

                    )
                }
        }
    }

    fun sendMessage(message: String) {
        // 유저의 메세지를 레디스 키를 통해 받아와서 생성
        val loginUserId = UserService.getLoginUserId()
        transaction {
            // 유저의 파티를 조회
            val row = PositionTable.selectAll()
                .where { PositionTable.assignedUserId eq loginUserId }
                .singleOrNull()
                ?: throw PartyBadRequest(ErrorCode.PARTY_NOT_FOUND)

            val foundPartyId = row[PositionTable.partyId].value
            // 유저 캐릭터 조회
            val findActiveCharacter = CharacterEntity.findActiveCharacter(loginUserId)
                ?: throw PartyBadRequest(ErrorCode.CHARACTER_NOT_FOUND)
            // redis로 부터 id 생성
            val partyChatId = partyRedisService.getPartyChatId(foundPartyId)

            // ChatTable insert
            val insertedMessage = PartyChatTable.insertReturning {
                it[id] = partyChatId
                it[partyId] = foundPartyId
                it[userId] = loginUserId
                it[characterName] = findActiveCharacter.name
                it[positionName] = row[PositionTable.name]
                it[content] = message
            }.singleOrNull()
                ?.let { PartyChatDto.from(it) }
                ?: throw PartyServerError(ErrorCode.INTERNAL_SERVER_ERROR, "", "Fail to save chat message", null)

            partyRedisService.publishMessage(PartyRedisService.partyChatTopic(foundPartyId), insertedMessage)
        }
    }
}
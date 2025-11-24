package taeyun.malanalter.party.character

import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Service
import taeyun.malanalter.config.exception.ErrorCode
import taeyun.malanalter.config.exception.PartyBadRequest
import taeyun.malanalter.party.character.dto.CharacterDto
import taeyun.malanalter.party.pat.dao.PartyEntity
import taeyun.malanalter.party.pat.dao.PositionTable
import taeyun.malanalter.user.UserService

@Service
class CharacterService {
    fun fetchAllUserCharacters(): List<CharacterDto> {
        val userId = UserService.getLoginUserId()
        return transaction {
            CharacterTable.selectAll()
                .where { CharacterTable.userId eq userId }
                .orderBy(CharacterTable.createdAt, SortOrder.ASC)
                .map { CharacterDto.from(it) }
                .toList()
        }
    }

    // 기본 캐릭터 정책:
    // 1. 유저당 기본 캐릭은 하나만 존재
    fun createCharacter(characterDto: CharacterDto): CharacterDto {
        val userId = UserService.getLoginUserId()
        transaction {
            // 이미 default 캐릭터가 존재하면 false 로 설정
            val isCharacterExist = CharacterTable.select(CharacterTable.id)
                .where { CharacterTable.userId eq userId }
                .count() > 0

            CharacterTable.insert {
                it[id] = characterDto.id.toString()
                it[name] = characterDto.name
                it[level] = characterDto.level
                it[job] = characterDto.job
                it[isActive] = !isCharacterExist
                it[comment] = characterDto.comment
                it[CharacterTable.userId] = userId
            }
        }
        return characterDto
    }

    fun deleteCharacter(characterId: String) {
        val userId = UserService.getLoginUserId()
        if(isParticipant(userId)){
            throw PartyBadRequest(ErrorCode.USER_ALREADY_IN_PARTY)
        }
        transaction {
            CharacterTable.deleteWhere { CharacterTable.userId eq userId and (CharacterTable.id eq characterId) }
        }
    }

    fun updateCharacter(characterId: String, characterDto: CharacterDto): CharacterDto {
        val userId = UserService.getLoginUserId()
        if(isParticipant(userId)){
            throw PartyBadRequest(ErrorCode.USER_ALREADY_IN_PARTY)
        }
        transaction {
            CharacterTable.update({ CharacterTable.userId eq userId and (CharacterTable.id eq characterId) }) {
                it[name] = characterDto.name
                it[level] = characterDto.level
                it[job] = characterDto.job
                it[comment] = characterDto.comment
            }
        }
        return characterDto
    }

    // 선택 캐릭터를 변경하는 API
    fun changeActiveCharacter(characterId: String) {
        val userId = UserService.getLoginUserId()
        transaction {
            // 1. 이미 파티를 생성중인지 체크
            PartyEntity.findByLeaderId(userId)?.let { party ->
                throw PartyBadRequest(ErrorCode.USER_ALREADY_IN_PARTY)
            }
            // 2. 이미 파티에 참여중인지 체크
            PositionTable.select(PositionTable.id).where { PositionTable.assignedUserId eq userId }.singleOrNull()
                ?.let { position ->
                    throw PartyBadRequest(ErrorCode.USER_ALREADY_IN_PARTY)
                }
            CharacterTable.update({ CharacterTable.userId eq userId }) {
                it[isActive] = false
            }
            CharacterTable.update({ CharacterTable.userId eq userId and (CharacterTable.id eq characterId) }) {
                it[isActive] = true
            }
        }
    }
    fun isParticipant(userId:Long): Boolean{
        return transaction {
            // 1. 이미 파티를 생성중인지 체크
            val isLeader =  PartyEntity.findByLeaderId(userId) != null
            // 2. 이미 파티에 참여중인지 체크
            val isRecruitee = PositionTable.select(PositionTable.id).where { PositionTable.assignedUserId eq userId }.singleOrNull() != null
            isLeader || isRecruitee
        }
    }

}
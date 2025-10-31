package taeyun.malanalter.party.character

import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import org.springframework.stereotype.Service
import taeyun.malanalter.party.character.dto.CharacterDto
import taeyun.malanalter.user.UserService

@Service
// todo : 캐릭터 생성 시 직업 검증 필요
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
    // 2. 캐릭터가 하나도 없을 때 생성하는 캐릭터는 무조건 기본 캐릭터로 설정
    fun createCharacter(characterDto: CharacterDto): CharacterDto {
        val userId = UserService.getLoginUserId()
        transaction {
            // 이미 default 캐릭터가 존재하면 기본캐릭터 변경
            val isDefaultExist = CharacterTable.selectAll().where { CharacterTable.userId eq userId and CharacterTable.isDefault }.count() > 0
            if(isDefaultExist && characterDto.isDefault) {
                CharacterTable.update({ CharacterTable.userId eq userId and (CharacterTable.isDefault eq true) }) {
                    it[isDefault] = false
                }
            }
            // 캐릭터가 하나도 없을 떄는 무조건 default 캐릭터로 설정
            CharacterTable.insert {
                it[id] = characterDto.id.toString()
                it[name] = characterDto.name
                it[level] = characterDto.level
                it[job] = characterDto.job
                it[isDefault] = if(isDefaultExist) characterDto.isDefault else true
                it[comment] = characterDto.comment
                it[CharacterTable.userId] = userId
            }
        }
        return characterDto
    }

    fun deleteCharacter(characterId: String) {
        val userId = UserService.getLoginUserId()
        transaction {
            CharacterTable.deleteWhere { CharacterTable.userId eq userId and (CharacterTable.id eq characterId.toString())  }
        }
    }

    // 기본 캐릭터를 없애는 변경은 할 수 없다. 무조건 하나의 캐릭터는 기본 캐릭터이도록 요청이 온다.
    // fe에서 select box 등으로 기본 캐릭터를 선택하게 하여 변경 요청을 보내도록 한다.
    fun updateCharacter(characterId: String, characterDto: CharacterDto): CharacterDto {
        val userId = UserService.getLoginUserId()
        transaction {
            if(characterDto.isDefault) {
                // 기본 캐릭터로 변경 요청 시 기존 기본 캐릭터를 해제
                CharacterTable.update({ CharacterTable.userId eq userId and (CharacterTable.isDefault eq true) }) {
                    it[isDefault] = false
                }
            }
            CharacterTable.update({ CharacterTable.userId eq userId and (CharacterTable.id eq characterId) }) {
                it[name] = characterDto.name
                it[level] = characterDto.level
                it[job] = characterDto.job
                it[isDefault] = characterDto.isDefault
                it[comment] = characterDto.comment
            }
        }
        return characterDto
    }

}
package taeyun.malanalter.party.character

import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass

class CharacterEntity(id: EntityID<String>) : Entity<String>(id) {
    companion object : EntityClass<String, CharacterEntity>(CharacterTable){
        fun findByUserAndCharacterId(userId:Long, characterId:String) : CharacterEntity?{
            return find{ CharacterTable.id eq characterId and  (CharacterTable.userId eq userId) }.singleOrNull()
        }
        // todo : character Id 를 파라미터로 받는 함수들을 변경
        fun findActiveCharacter(userId:Long) : CharacterEntity?{
            return find { CharacterTable.userId eq userId and CharacterTable.isActive }.singleOrNull()
        }
    }

    var name by CharacterTable.name
    var level by CharacterTable.level
    var job by CharacterTable.job
    var comment by CharacterTable.comment
    val userId by CharacterTable.userId

}
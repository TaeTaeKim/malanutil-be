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
    }

    var name by CharacterTable.name
    var level by CharacterTable.level
    var job by CharacterTable.job
    var comment by CharacterTable.comment
    val userId by CharacterTable.userId

}
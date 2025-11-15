package taeyun.malanalter.party.pat.dao

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime
import taeyun.malanalter.party.character.CharacterTable
import taeyun.malanalter.user.domain.Users

object PositionTable: IdTable<String>("position") {
    override val id = varchar("id", 255).entityId()
    val partyId = reference("party_id", PartyTable.id, onDelete = ReferenceOption.CASCADE).index()
    val name = varchar("name", 100) // "1층", "좌우깐", etc.
    val description = varchar("description", 500).nullable() // 120히어로 (구인 완료 된 경우).
    val price = varchar("price", 30).nullable()
    val isLeader = bool("is_leader").default(false)
    val status = enumerationByName<PositionStatus>("status", 50).default(PositionStatus.RECRUITING)
    val isPriestSlot = bool("is_priest_slot").default(false)
    val preferJob = varchar("prefer_job", 255).nullable()

    // Who filled this position
    val assignedUserId = reference("assigned_user_id", Users.id, onDelete = ReferenceOption.SET_NULL).nullable()
    val assignedCharacterId = reference("assigned_character_id", CharacterTable.id, onDelete = ReferenceOption.SET_NULL).nullable()

    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)


    override val primaryKey = PrimaryKey(id)

}

enum class PositionStatus {
    RECRUITING,
    COMPLETED
}
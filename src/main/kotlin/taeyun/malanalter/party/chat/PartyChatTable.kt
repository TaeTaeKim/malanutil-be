package taeyun.malanalter.party.chat

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.timestamp
import taeyun.malanalter.party.pat.dao.PartyTable
import taeyun.malanalter.user.domain.Users

object PartyChatTable: IdTable<String>(name = "party_chat") {
    override val id: Column<EntityID<String>> = varchar("chat_id", 255).entityId()
    val partyId = reference("party_id", PartyTable, onDelete = ReferenceOption.CASCADE).index()
    val userId = reference("user_id", Users.id, onDelete = ReferenceOption.SET_NULL).nullable()
    val characterName = varchar("character_name", 255)
    val positionName = varchar("position_name", 255)
    val content = text("content")
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)

    init {
        index(isUnique = false, partyId, createdAt)
    }
}
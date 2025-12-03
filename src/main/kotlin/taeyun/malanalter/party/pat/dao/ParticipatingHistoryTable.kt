package taeyun.malanalter.party.pat.dao

import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime

object ParticipatingHistoryTable : LongIdTable(name = "participating_history") {
    val participant = long("participant").index()
    val leaderId = long("leader_id").index()
    val mapId = long("map_id")
    val description = varchar("description", 255)
    val leaderCharacterName = varchar("leader_character_name", 50)
    val partyId = varchar("party_id", 255).index()
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
}
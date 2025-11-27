package taeyun.malanalter.party.pat.dao

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime
import taeyun.malanalter.party.character.CharacterTable
import taeyun.malanalter.user.domain.Users

object PartyTable : IdTable<String>(name = "party") {
    override val id: Column<EntityID<String>> = varchar("party_id", 255).entityId()

    val mapId = long("map_id")
    val hasPosition = bool("has_position")
    val description = varchar("description", 255)
    val numPeople = integer("num_people")
    val channel = varchar("channel", 50)
    val partyShortId = varchar("party_short_id", 10).nullable().index()

    val leaderId  = reference("leader_id", Users.id)
    val leaderCharacter = reference("leader_character", CharacterTable.id)
    val discordNotification = bool("discord_notification")
    val status = enumerationByName<PartyStatus>(name = "party_status", length = 50).default(PartyStatus.RECRUITING)

    val lastHeartbeatAt = datetime(name = "last_heartbeat_at").defaultExpression(CurrentDateTime)
    val inactiveSince = datetime("inactive_since").nullable()

    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(id)
    init {
        // Fast query: "parties inactive for > 1 hour"
        index(isUnique = false, status, inactiveSince)

        // Fast query: "active parties for map"
        index(isUnique = false, mapId, status)

        // Fast query: "parties created by leader"
        index(isUnique = false, leaderId)
    }

}
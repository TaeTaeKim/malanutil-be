package taeyun.malanalter.party.pat.dao

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime
import org.jetbrains.exposed.v1.json.jsonb
import taeyun.malanalter.party.pat.dto.PartyCreate
import taeyun.malanalter.user.domain.Users


val mapper = jacksonObjectMapper()
object PartyHistory : LongIdTable(name = "party_create_history") {
    val userId = reference("user_id", Users.id).index()
    val mapId = long("map_id").index()
    val partyData = jsonb<PartyCreate>(
        "party_data",
        { mapper.writeValueAsString(it) },
        { mapper.readValue(it, PartyCreate::class.java) }
    )
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)

    init {
        index(isUnique = true, userId, mapId)
    }
}
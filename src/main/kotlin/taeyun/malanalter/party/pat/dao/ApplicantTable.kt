package taeyun.malanalter.party.pat.dao

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import taeyun.malanalter.party.character.CharacterTable

object ApplicantTable: UUIDTable(name = "applicant") {
    val partyId = reference("party_id", PartyTable, onDelete = ReferenceOption.CASCADE)
    val positionId = reference("position_id", PositionTable, onDelete = ReferenceOption.CASCADE)
    val characterId = reference("character_id", CharacterTable, onDelete = ReferenceOption.CASCADE)

    init {
        uniqueIndex(partyId, positionId, characterId)
    }
}
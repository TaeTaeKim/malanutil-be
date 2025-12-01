package taeyun.malanalter.party.pat.dao

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import taeyun.malanalter.party.character.CharacterTable
import taeyun.malanalter.user.domain.Users

object ApplicantTable: UUIDTable(name = "party_applicant") {
    val partyId = reference("party_id", PartyTable, onDelete = ReferenceOption.CASCADE)
    val positionId = reference("position_id", PositionTable, onDelete = ReferenceOption.CASCADE)
    val characterId = reference("character_id", CharacterTable, onDelete = ReferenceOption.CASCADE)
    val applyUserId = reference("apply_user_id", Users, onDelete = ReferenceOption.CASCADE).index()

    init {
        uniqueIndex(partyId, characterId)
    }
}
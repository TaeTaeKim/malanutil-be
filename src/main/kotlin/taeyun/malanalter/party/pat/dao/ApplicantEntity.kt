package taeyun.malanalter.party.pat.dao

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.jetbrains.exposed.v1.dao.UUIDEntityClass
import taeyun.malanalter.party.character.CharacterEntity
import java.util.*

class ApplicantEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ApplicantEntity>(ApplicantTable)

    var partyId by ApplicantTable.partyId
    var positionId by ApplicantTable.positionId
    var characterId by ApplicantTable.characterId
    val character by CharacterEntity referencedOn ApplicantTable.characterId
}
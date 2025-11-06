package taeyun.malanalter.party.pat.dao

import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.jetbrains.exposed.v1.dao.UUIDEntityClass
import java.util.*

class ApplicantEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ApplicantEntity>(ApplicantTable){
        fun existBy(partyId: String, positionId: String, characterId: String): Boolean{
            return find { ApplicantTable.partyId eq partyId and
                    (ApplicantTable.positionId eq positionId) and
                    (ApplicantTable.characterId eq characterId)}
                .empty().not()
        }
    }

    var partyId by ApplicantTable.partyId
    var positionId by ApplicantTable.positionId
    var characterId by ApplicantTable.characterId

}
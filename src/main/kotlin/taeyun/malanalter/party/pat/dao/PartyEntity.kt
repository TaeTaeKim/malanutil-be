package taeyun.malanalter.party.pat.dao

import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass

class PartyEntity(id: EntityID<String>): Entity<String>(id) {
    companion object : EntityClass<String, PartyEntity>(PartyTable){
        fun findActivePartyByLeaderId(leaderId: Long): PartyEntity? {
            return find { PartyTable.leaderId eq leaderId and (PartyTable.status eq PartyStatus.RECRUITING) }.singleOrNull()
        }

        fun findByLeaderId(leaderId: Long): PartyEntity? {
            return find { PartyTable.leaderId eq leaderId }.singleOrNull()
        }
    }

    var status by PartyTable.status
    val mapId by PartyTable.mapId

}
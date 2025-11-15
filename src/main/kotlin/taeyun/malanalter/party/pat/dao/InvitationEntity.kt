package taeyun.malanalter.party.pat.dao

import kotlinx.datetime.toJavaLocalDateTime
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.UUIDEntity
import org.jetbrains.exposed.v1.dao.UUIDEntityClass
import java.time.LocalDateTime
import java.util.*

const val INVITATION_PENDING_DURATION_MIN = 5
class InvitationEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<InvitationEntity>(Invitation){
        fun alreadyInvited(partyId: String, invitedUserId: Long): Boolean {
            return find { Invitation.partyId eq partyId and (Invitation.invitedUserId eq invitedUserId) }
                .count { it.isPending() } > 0
        }
        fun getInvitedTimeByPartyId(partyId:String) :Map<Long, LocalDateTime>{
            return find{ Invitation.partyId eq partyId }.filter{it.isPending()}.associate { it.invitedUserId to it.invitedAt.toJavaLocalDateTime() }
        }
    }

    var partyId by Invitation.partyId
    var invitedUserId by Invitation.invitedUserId
    var invitedAt by Invitation.invitedAt


    fun isPending(): Boolean{
        val pendingThreshold = invitedAt.toJavaLocalDateTime().plusMinutes(INVITATION_PENDING_DURATION_MIN.toLong())
        return pendingThreshold.isAfter(LocalDateTime.now())
    }
}
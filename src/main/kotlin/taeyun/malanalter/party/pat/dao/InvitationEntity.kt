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
        fun invitedUserIdByParty(partyId: String): List<Long>{
            return find { Invitation.partyId eq partyId }
                .map{it.invitedUserId.value}
        }
        fun findById(invitationId: String) : InvitationEntity? {
            return findById(UUID.fromString(invitationId))
        }
        fun changeStatus(invitationId: String, newStatus: InvitationStatus){
            val invitation = findById(invitationId)
            invitation?.status = newStatus
        }
    }

    var partyId by Invitation.partyId
    var invitedUserId by Invitation.invitedUserId
    var invitedAt by Invitation.invitedAt
    var positionId by Invitation.positionId
    var status by Invitation.status


    fun isPending(): Boolean{
        val pendingThreshold = invitedAt.toJavaLocalDateTime().plusMinutes(INVITATION_PENDING_DURATION_MIN.toLong())
        return pendingThreshold.isAfter(LocalDateTime.now())
    }
}
package taeyun.malanalter.party.pat.dao

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime
import taeyun.malanalter.user.domain.Users

object Invitation : UUIDTable(name = "party_invitations") {
    val partyId = reference("party_id", PartyTable.id, onDelete = ReferenceOption.CASCADE).index()
    val positionId = reference("position_id", PositionTable.id, onDelete = ReferenceOption.CASCADE)
    val invitedUserId = reference("invited_user_id", Users, onDelete = ReferenceOption.CASCADE).index()
    val invitedAt = datetime("invited_at").defaultExpression(CurrentDateTime)
    val rejected = bool("rejected").default(false)


    init {
        index(isUnique = false, partyId, invitedUserId) // 파티가 이미 초대보낸 사람 조회시 사용
    }
}
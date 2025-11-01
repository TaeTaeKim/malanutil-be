package taeyun.malanalter.party.pat.dao

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime

object Invitation : UUIDTable(name = "party_invitations") {
    val partyId = reference("party_id", PartyTable.id, onDelete = ReferenceOption.CASCADE).index()
    val invitedUserId = long("invited_user_id").index()
    val invitedAt = datetime("invited_at").defaultExpression(CurrentDateTime)

    init {
        index(isUnique = false, partyId, invitedUserId) // 파티가 이미 초대보낸 사람 조회시 사용
    }
}
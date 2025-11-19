package taeyun.malanalter.party.pat.dto

import kotlinx.datetime.toJavaLocalDateTime
import org.jetbrains.exposed.v1.core.ResultRow
import taeyun.malanalter.party.pat.dao.Invitation
import taeyun.malanalter.party.pat.dao.PartyTable
import taeyun.malanalter.party.pat.dao.PositionTable
import java.time.LocalDateTime

data class InvitationDto(
    val invitationId: String,
    val partyId: String,
    val partyDesc: String,
    val positionId: String,
    val positionName: String,
    val positionPrice: String?,
    val mapId: Long,
    val createdAt: LocalDateTime,
){
    companion object{
        fun from(row: ResultRow): InvitationDto {
            return InvitationDto(
                invitationId = row[Invitation.id].value.toString(),
                partyId = row[PartyTable.id].value,
                partyDesc = row[PartyTable.description],
                positionId = row[PositionTable.id].value,
                positionName = row[PositionTable.name],
                positionPrice = row[PositionTable.price],
                mapId = row[PartyTable.mapId],
                createdAt = row[Invitation.invitedAt].toJavaLocalDateTime(),
            )
        }
    }
}

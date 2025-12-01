package taeyun.malanalter.party.pat.dto

import org.jetbrains.exposed.v1.core.ResultRow
import taeyun.malanalter.party.pat.dao.PositionStatus
import taeyun.malanalter.party.pat.dao.PositionTable

// Response DTO for Position information
data class PositionDto(
    val id: String,
    val partyId: String,
    val name: String, // "1층", "좌우깐"
    val description: String?, // "120히어로" , "110프리"
    val price: String?, // 심10+지참10
    val isLeader: Boolean,
    val status: PositionStatus, // RECRUITING, COMPLETED
    val isPriestSlot: Boolean,
    val preferJob : List<String> = emptyList(),
    val orderNum: Int,

    // Assigned user info (null if position is empty)
    val assignedUserId: Long?,
    val assignedCharacterId: String?,
    val assignedCharacterName: String?,
) {
    companion object {
        // Convert database ResultRow to PositionDto
        fun from(row: ResultRow): PositionDto {
            return PositionDto(
                id = row[PositionTable.id].value,
                partyId = row[PositionTable.partyId].value,
                name = row[PositionTable.name],
                description = row[PositionTable.description],
                price = row[PositionTable.price],
                isLeader = row[PositionTable.isLeader],
                status = row[PositionTable.status],
                isPriestSlot = row[PositionTable.isPriestSlot],
                preferJob = row[PositionTable.preferJob]?.split(",") ?: emptyList(),
                assignedUserId = row[PositionTable.assignedUserId]?.value,
                assignedCharacterId = row[PositionTable.assignedCharacterId]?.value,
                assignedCharacterName = row[PositionTable.assignedCharacterName],
                orderNum = row[PositionTable.orderNumber]
            )
        }
    }
}
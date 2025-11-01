package taeyun.malanalter.party.pat.dto

import org.jetbrains.exposed.v1.core.ResultRow
import taeyun.malanalter.party.pat.dao.PositionStatus
import taeyun.malanalter.party.pat.dao.PositionTable

// Response DTO for Position information
data class PositionDto(
    val id: String,
    val partyId: String,
    val name: String, // "1층", "좌우깐"
    val description: String, // "심비 1억", "지참금 없음"
    val isLeader: Boolean,
    val status: PositionStatus, // RECRUITING, COMPLETED
    val isPriestSlot: Boolean,
    val preferJob : List<String> = emptyList(),

    // Assigned user info (null if position is empty)
    val assignedUserId: Long?,
    val assignedCharacterId: String?
) {
    companion object {
        // Convert database ResultRow to PositionDto
        fun from(row: ResultRow): PositionDto {
            return PositionDto(
                id = row[PositionTable.id].value,
                partyId = row[PositionTable.partyId].value,
                name = row[PositionTable.name],
                description = row[PositionTable.description],
                isLeader = row[PositionTable.isLeader],
                status = row[PositionTable.status],
                isPriestSlot = row[PositionTable.isPriestSlot],
                preferJob = row[PositionTable.preferJob]?.split(",") ?: emptyList(),
                assignedUserId = row[PositionTable.assignedUserId]?.value,
                assignedCharacterId = row[PositionTable.assignedCharacterId]?.value
            )
        }

        // Convert Position (request DTO) to PositionDto (response DTO)
        // Used when creating a new position
        fun from(
            position: Position,
            positionId: String,
            partyId: String,
            assignedUserId: Long?,
            assignedCharacterId: String?
        ): PositionDto {
            return PositionDto(
                id = positionId,
                partyId = partyId,
                name = position.name,
                description = position.description,
                isLeader = position.isLeader,
                status = position.status,
                isPriestSlot = position.isPriestSlot,
                preferJob = position.preferJob,
                assignedUserId = assignedUserId,
                assignedCharacterId = assignedCharacterId
            )
        }
    }
}
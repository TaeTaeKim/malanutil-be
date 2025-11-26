package taeyun.malanalter.party.pat.dto

import kotlinx.datetime.toJavaLocalDateTime
import org.jetbrains.exposed.v1.core.ResultRow
import taeyun.malanalter.party.pat.dao.PartyStatus
import taeyun.malanalter.party.pat.dao.PartyTable
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

// Response DTO for Party information
data class PartyResponse(
    val id: String,
    val mapCode: Long,
    val hasPosition: Boolean, // 포지션이 있는 파티 여부 (개미굴 등은 없다)
    val description: String,
    val numPeople: Int,
    val channel: String,

    // Leader information
    val leaderId: Long,
    val leaderCharacterId: String,

    // Party status
    val status: PartyStatus, // RECRUITING, FULLED, INACTIVE

    // Positions (empty list if hasPosition = false)
    val positions: List<PositionDto>,

    // Metadata
    val createdAt: Instant,
    val updatedAt: LocalDateTime
) {
    companion object {
        // Convert database ResultRow to PartyResponse (without positions)
        fun from(row: ResultRow): PartyResponse {
            return PartyResponse(
                id = row[PartyTable.id].value,
                mapCode = row[PartyTable.mapId],
                hasPosition = row[PartyTable.hasPosition],
                description = row[PartyTable.description],
                numPeople = row[PartyTable.numPeople],
                channel = row[PartyTable.channel],
                leaderId = row[PartyTable.leaderId].value,
                leaderCharacterId = row[PartyTable.leaderCharacter].value,
                status = row[PartyTable.status],
                positions = emptyList(), // Positions should be loaded separately
                createdAt = row[PartyTable.createdAt].toJavaLocalDateTime().toInstant(ZoneOffset.UTC),
                updatedAt = row[PartyTable.updatedAt].toJavaLocalDateTime()
            )
        }

        // Create PartyResponse with positions
        fun withPositions(row: ResultRow, positions: List<PositionDto>): PartyResponse {
            return from(row).copy(positions = positions)
        }

        fun fromJoinedRow(row: List<ResultRow>): PartyResponse {
            val positionDtoList = row.map { PositionDto.from(it) }
            val partyRow = row.first()
            return withPositions(partyRow, positionDtoList)
        }
    }
}

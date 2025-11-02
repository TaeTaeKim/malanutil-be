package taeyun.malanalter.party.pat.dto

import java.time.Instant

/**
 * 파티장을 위한 인재풀 DTO
 * @see TalentDto 에서 lastSent가 추가 되었음.
 */
data class TalentResponse(
    val userId: Long,
    val characterId: String,
    val lastSent: Instant,
    val level: Int,
    val job: String,
    val comment: String
)
package taeyun.malanalter.party.pat.dto

/**
 * 파티장이 지원을 수락할 때 사용하는 요청 DTO
 */
data class ApplyAcceptReq(
    val partyId: String,
    val positionId: String,
    val applyId: String,
    val applicantUserId: String,
    val applicantCharacterId: String,
    val mapId: Long
)

package taeyun.malanalter.party.pat.dto

data class ApplyAcceptReq(
    val partyId: String,
    val positionId: String,
    val applyId: String,
    val applicantUserId: String,
    val applicantCharacterId: String,
    val applicantLevel: Int,
    val applicantJob: String,
    val mapId: Long
)

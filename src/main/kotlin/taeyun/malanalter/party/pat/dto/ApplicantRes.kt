package taeyun.malanalter.party.pat.dto

data class ApplicantRes(
    val applyId: String,
    val applyUserId: String,
    val characterId: String,
    val name: String,
    val level: Int,
    val job: String,
    val comment: String?,
    val positionId: String,
)

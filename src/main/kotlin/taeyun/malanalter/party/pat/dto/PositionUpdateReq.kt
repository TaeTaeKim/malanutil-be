package taeyun.malanalter.party.pat.dto

import taeyun.malanalter.party.pat.dao.PositionStatus

data class PositionUpdateReq(
    val name: String,
    val price: String?,
    val preferJob: List<String>?,
    val status: PositionStatus,
    val recruitedJob: String?,
    val recruitedLevel: Int?,
    val mapCode: Long
)

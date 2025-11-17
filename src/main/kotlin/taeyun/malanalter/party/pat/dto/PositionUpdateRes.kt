package taeyun.malanalter.party.pat.dto

import taeyun.malanalter.party.pat.dao.PositionStatus

data class PositionUpdateRes(
    val partyId: String,
    val positionId: String,
    val name: String,
    val price: String?,
    val preferJobs: List<String>,
    val status: PositionStatus,
    val description: String?,
)
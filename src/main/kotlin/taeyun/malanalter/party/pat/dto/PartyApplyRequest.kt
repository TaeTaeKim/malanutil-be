package taeyun.malanalter.party.pat.dto

data class PartyApplyRequest(
    val partyId: String,
    val positionId: String,
    val positionName: String,
    val characterId: String,
)

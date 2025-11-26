package taeyun.malanalter.party.pat.dto

data class PartyWithRoleResponse(
    val party : PartyResponse,
    val role: PartyRole
)
enum class PartyRole{
    LEADER, MEMBER
}

package taeyun.malanalter.party.pat.dto

data class RegisteringPoolResponse(
    val mapIds: List<Long>,
    val remainSec: Long
){
    companion object{
        fun makeExpired(): RegisteringPoolResponse {
            return RegisteringPoolResponse(
                mapIds = emptyList(),
                remainSec = 0
            )
        }
    }
}

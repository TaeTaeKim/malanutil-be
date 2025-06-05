package taeyun.malanalter.auth.dto

import jakarta.validation.constraints.NotBlank

data class RefreshRequest (
    @field:NotBlank
    private val username: String?,
    @field:NotBlank
    val refreshToken: String?
){
    fun getUsername():Long{
        return username!!.toLong()
    }
}
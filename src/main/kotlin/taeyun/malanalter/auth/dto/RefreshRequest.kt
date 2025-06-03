package taeyun.malanalter.auth.dto

import jakarta.validation.constraints.NotBlank

data class RefreshRequest (
    @field:NotBlank
    val username: String?,
    @field:NotBlank
    val refreshToken: String?
)
package taeyun.malanalter.user.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

data class UserRegisterRequest (
    @field:NotBlank(message = "Username must not be blank")
    val username: String,
    @field:Min(value = 6, message = "Password must be at least 6 characters long")
    val password: String
)
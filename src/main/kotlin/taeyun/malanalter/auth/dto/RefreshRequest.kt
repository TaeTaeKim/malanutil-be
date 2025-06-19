package taeyun.malanalter.auth.dto

import jakarta.validation.constraints.NotNull

data class RefreshRequest (
    @field:NotNull(message = "UserId 는 Null일 수 없습니다.")
    val userId: Long?,
    val refreshToken: String?
)
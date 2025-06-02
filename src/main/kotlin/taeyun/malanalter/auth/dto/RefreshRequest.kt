package taeyun.malanalter.auth.dto

data class RefreshRequest (
    val username: String,
    val refreshToken: String
)
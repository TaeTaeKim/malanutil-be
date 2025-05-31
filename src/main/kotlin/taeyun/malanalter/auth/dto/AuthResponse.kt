package taeyun.malanalter.auth.dto

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val username: String,
    val type: String = "Bearer",
    val expireAt: Long
)

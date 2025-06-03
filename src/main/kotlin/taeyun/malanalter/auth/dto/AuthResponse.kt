
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val type: String = "Bearer",
    val expireAt: Long
)

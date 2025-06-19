
data class AuthResponse(
    val accessToken: String,
    val type: String = "Bearer",
    val expireAt: Long
)

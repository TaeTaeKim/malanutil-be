package taeyun.malanalter.auth

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties(prefix = "jwt")
data class AuthProperties @ConstructorBinding constructor(
    val secretKey: String,
    val accessTokenExpireDay: Long,
    val refreshTokenExpireDay: Long
)

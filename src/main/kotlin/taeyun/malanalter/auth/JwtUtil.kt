package taeyun.malanalter.auth

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtUtil(val authProperties: AuthProperties) {
    private val key: SecretKey by lazy{
        Keys.hmacShaKeyFor(authProperties.secretKey.toByteArray())
    }


    fun generateAccessToken(username: String): String {
        return Jwts.builder()
            .subject(username)
            .issuedAt(Date())
            .expiration(getExpiry(authProperties.accessTokenExpireDay))
            .claim("type", "access")
            .signWith(key)
            .compact()
    }

    fun generateRefreshToken(): String {
        return Jwts.builder()
            .subject(UUID.randomUUID().toString())
            .issuedAt(Date())
            .expiration(getExpiry(authProperties.refreshTokenExpireDay))
            .claim("type", "refresh")
            .signWith(key)
            .compact()
    }

    fun getExpiryFromToken(token: String): Date? {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
            .expiration
    }
    private fun getExpiry(days: Long): Date{
        return Date(LocalDateTime.now().plusDays(days).toEpochSecond(ZoneOffset.UTC) * 1000)
    }
}
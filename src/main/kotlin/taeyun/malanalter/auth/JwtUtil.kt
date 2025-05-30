package taeyun.malanalter.auth

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.security.Key
import java.util.*

@Component
class JwtUtil(val authProperties: AuthProperties) {
    private val key: Key by lazy{
        Keys.hmacShaKeyFor(authProperties.secretKey.toByteArray())
    }


    fun generateAccessToken(username: String): String {
        return Jwts.builder()
            .subject(username)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + authProperties.accessTokenExpirationTime))
            .claim("type", "access")
            .signWith(key)
            .compact()
    }

    fun generateRefreshToken(): String {
        return Jwts.builder()
            .subject(UUID.randomUUID().toString())
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + authProperties.refreshTokenExpirationTime))
            .claim("type", "refresh")
            .signWith(key)
            .compact()
    }
}
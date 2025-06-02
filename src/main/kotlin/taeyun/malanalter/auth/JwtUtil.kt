package taeyun.malanalter.auth

import io.github.oshai.kotlinlogging.KotlinLogging
import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import taeyun.malanalter.config.exception.AlerterJwtException
import taeyun.malanalter.config.exception.ErrorCode
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import javax.crypto.SecretKey

private val logger = KotlinLogging.logger{}
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

    private fun getExpiry(days: Long): Date{
        return Date(LocalDateTime.now().plusDays(days).toEpochSecond(ZoneOffset.UTC) * 1000)
    }
    fun getExpiryFromToken(token: String): Date {
        return extractClaim(token, Claims::getExpiration)
    }
    fun getUsername(token: String) : String{
        return extractClaim(token, Claims::getSubject)
    }

    fun isExpiredToken(token: String): Boolean {
        return extractClaim(token, Claims::getExpiration).before(Date())
    }

    private fun <T> extractClaim(token: String, claimResolver: (Claims) -> T) : T{
        try {

            val claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload
            return claimResolver(claims)
        } catch (e: JwtException) {
            val randomUUID = UUID.randomUUID()
            logger.error { "$randomUUID ${e.message}" }
            throw AlerterJwtException(ErrorCode.INVALID_TOKEN, "[$randomUUID] Error in extracting claim")
        }
    }
}
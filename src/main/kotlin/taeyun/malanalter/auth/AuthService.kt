package taeyun.malanalter.auth


import AuthResponse
import kotlinx.datetime.toKotlinLocalDateTime
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Service
import taeyun.malanalter.auth.domain.RefreshToken
import taeyun.malanalter.config.exception.AlerterJwtException
import taeyun.malanalter.config.exception.AlerterNotFoundException
import taeyun.malanalter.config.exception.ErrorCode
import taeyun.malanalter.user.domain.UserEntity

@Service
class AuthService(
    val authProperties: AuthProperties,
    val jwtUtil: JwtUtil
) {

    fun registerRefreshToken(user: UserEntity, refreshToken: String) {
        transaction {
            RefreshToken.new {
                this.userId = user.id.value
                this.token = refreshToken
                this.expiredAt = java.time.LocalDateTime.now()
                    .plusDays(authProperties.refreshTokenExpireDay)
                    .toKotlinLocalDateTime()
            }
        }
    }

    fun renewToken(foundUser: UserEntity, refreshToken: String): AuthResponse {
         return transaction {
            RefreshToken.findRefreshTokenByUserId(foundUser.userId.value, refreshToken)?.let { foundToken ->
                if (foundToken.isExpired() && foundToken.isRevoked) {
                    throw AlerterJwtException(ErrorCode.EXPIRED_TOKEN, "Refresh Token Expired")
                }
                // 기존 리프레시 토큰 삭제
                foundToken.delete()

                // 새로운 refresh token 생성 후 등록
                val generateRefreshToken = jwtUtil.generateRefreshToken()
                registerRefreshToken(foundUser, generateRefreshToken)
                val generateAccessToken = jwtUtil.generateAccessToken(foundUser.username)
                AuthResponse(
                    accessToken = generateAccessToken,
                    refreshToken = generateRefreshToken,
                    expireAt = jwtUtil.getExpiryFromToken(generateAccessToken).toInstant().epochSecond
                )
            }?: throw AlerterNotFoundException(ErrorCode.REFRESH_TOKEN_NOT_FOUND)
        }
    }
}
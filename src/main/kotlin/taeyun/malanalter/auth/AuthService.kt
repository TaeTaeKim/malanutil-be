package taeyun.malanalter.auth


import AuthResponse
import kotlinx.datetime.toKotlinLocalDateTime
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Service
import taeyun.malanalter.auth.domain.RefreshToken
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
                    throw IllegalArgumentException("Refresh Token Expired") // fixme: change to jwt expired
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
            }?: throw IllegalArgumentException("Can't found RefreshToken") // fixme: change to code that send to login
        }
    }
}
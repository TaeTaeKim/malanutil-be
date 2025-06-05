package taeyun.malanalter.auth


import AuthResponse
import kotlinx.datetime.toKotlinLocalDateTime
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Service
import taeyun.malanalter.auth.domain.LogoutToken
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

    fun registerRefreshToken(userId: Long, refreshToken: String) {
        transaction {
            RefreshToken.deleteByUserId(userId)
        }
        transaction {
            RefreshToken.new {
                this.userId = userId
                this.token = refreshToken
                this.expiredAt = java.time.LocalDateTime.now()
                    .plusDays(authProperties.refreshTokenExpireDay)
                    .toKotlinLocalDateTime()
            }
        }
    }

    fun logout(accessToken: String) {
        val userId = jwtUtil.getUserFromExpiredToken(accessToken)
        transaction {
            // logout token 에 추가
            LogoutToken.new {
                this.logoutToken = accessToken
            }
            val findUserEntity = UserEntity.findById(userId) ?: throw AlerterNotFoundException(
                ErrorCode.USER_NOT_FOUND,
                "Logout User Not found"
            )
            RefreshToken.deleteByUserId(findUserEntity.id.value)
        }
    }

    fun renewToken(foundUser: UserEntity, refreshToken: String): AuthResponse {
        return transaction {
            RefreshToken.findRefreshTokenByUserId(foundUser.id.value, refreshToken)?.let { foundToken ->
                if (foundToken.isExpired() && foundToken.isRevoked) {
                    throw AlerterJwtException(ErrorCode.EXPIRED_REFRESH_TOKEN, "Refresh Token Expired")
                }
                // 기존 리프레시 토큰 삭제
                foundToken.delete()

                // 새로운 refresh token 생성 후 등록
                val generateRefreshToken = jwtUtil.generateRefreshToken()
                registerRefreshToken(foundUser.id.value, generateRefreshToken)
                val generateAccessToken = jwtUtil.generateAccessToken(foundUser.id.value)
                AuthResponse(
                    accessToken = generateAccessToken,
                    refreshToken = generateRefreshToken,
                    expireAt = jwtUtil.getExpiryFromToken(generateAccessToken).toInstant().epochSecond
                )
            } ?: throw AlerterNotFoundException(ErrorCode.REFRESH_TOKEN_NOT_FOUND)
        }
    }

}
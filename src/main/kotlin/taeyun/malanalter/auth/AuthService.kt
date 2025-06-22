package taeyun.malanalter.auth


import AuthResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletResponse
import kotlinx.datetime.toKotlinLocalDateTime
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Service
import taeyun.malanalter.auth.domain.LogoutToken
import taeyun.malanalter.auth.domain.RefreshToken
import taeyun.malanalter.config.exception.AlerterJwtException
import taeyun.malanalter.config.exception.AlerterNotFoundException
import taeyun.malanalter.config.exception.ErrorCode
import taeyun.malanalter.user.domain.UserEntity

private val logger = KotlinLogging.logger {  }
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

    /**
     * 액세스 토큰 만료시 refreshToken 과 함께 액세스 토큰 재발급하는 메소드
     */
    fun renewToken(foundUser: UserEntity, refreshToken: String, response: HttpServletResponse): AuthResponse {
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
                response.addCookie(AlerterCookieUtil.makeRefreshTokenCookie(generateRefreshToken))
                logger.info { "${foundUser.username} 리프레시 토큰 쿠키 전환됨." }
                val generateAccessToken = jwtUtil.generateAccessToken(foundUser.id.value)
                AuthResponse(
                    accessToken = generateAccessToken,
                    expireAt = jwtUtil.getExpiryFromToken(generateAccessToken).toInstant().epochSecond
                )
            } ?: throw AlerterNotFoundException(ErrorCode.REFRESH_TOKEN_NOT_FOUND)
        }
    }

}
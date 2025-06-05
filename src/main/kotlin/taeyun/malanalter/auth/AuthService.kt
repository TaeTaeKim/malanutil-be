package taeyun.malanalter.auth


import AuthResponse
import kotlinx.datetime.toKotlinLocalDateTime
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import taeyun.malanalter.auth.domain.LogoutToken
import taeyun.malanalter.auth.domain.RefreshToken
import taeyun.malanalter.auth.dto.LoginRequest
import taeyun.malanalter.config.exception.AlerterBadRequest
import taeyun.malanalter.config.exception.AlerterJwtException
import taeyun.malanalter.config.exception.AlerterNotFoundException
import taeyun.malanalter.config.exception.ErrorCode
import taeyun.malanalter.user.UserService
import taeyun.malanalter.user.domain.UserEntity

@Service
class AuthService(
    val authProperties: AuthProperties,
    val jwtUtil: JwtUtil,
    val passwordEncoder: PasswordEncoder
) {

    fun registerRefreshToken(user: UserEntity, refreshToken: String) {
        transaction {
            RefreshToken.deleteByUserId(user.id.value)
        }
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

    fun logout(accessToken: String) {
        val userId = jwtUtil.getUserFromExpiredToken(accessToken)
        transaction {
            // logout token 에 추가
            LogoutToken.new {
                this.logoutToken = accessToken
            }
            val findUserEntity = UserEntity.findById(userId)
            RefreshToken.deleteByUserId(UserService.getLoginUserId())
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
                registerRefreshToken(foundUser, generateRefreshToken)
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
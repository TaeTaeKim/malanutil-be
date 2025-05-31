package taeyun.malanalter.auth.dto


import kotlinx.datetime.toKotlinLocalDateTime
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Service
import taeyun.malanalter.auth.AuthProperties
import taeyun.malanalter.auth.domain.RefreshToken
import taeyun.malanalter.user.domain.UserEntity

@Service
class AuthService(
    val authProperties: AuthProperties
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
}
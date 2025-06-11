package taeyun.malanalter.user

import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import taeyun.malanalter.auth.AlerterUserPrincipal
import taeyun.malanalter.auth.discord.DiscordOAuth2User
import taeyun.malanalter.auth.domain.LogoutToken
import taeyun.malanalter.config.exception.AlerterJwtException
import taeyun.malanalter.config.exception.AlerterNotFoundException
import taeyun.malanalter.config.exception.AlerterServerError
import taeyun.malanalter.config.exception.ErrorCode
import taeyun.malanalter.user.domain.UserEntity
import taeyun.malanalter.user.domain.Users
import taeyun.malanalter.user.dto.AlarmTimeRequest
import taeyun.malanalter.user.dto.LoginUser
import java.util.*

@Service
class UserService {

    companion object {
        fun getLoginUserId(): Long {
            val authentication = SecurityContextHolder.getContext().authentication
            if (authentication == null || !authentication.isAuthenticated) {
                throw AlerterJwtException(ErrorCode.INVALID_TOKEN, "Not Authenticated User")
            }

            return when (val principal = authentication.principal) {
                is AlerterUserPrincipal -> principal.userId
                else -> {
                    throw AlerterServerError(
                        uuid = UUID.randomUUID().toString(),
                        message = "잘못된 Authentication 입니다.",
                        rootCause = null
                    )
                }
            }
        }
    }

    fun existById(userId: Long): Boolean {
        return transaction {
            UserEntity.findById(userId) != null
        }
    }


    fun addLoginUser(discordOAuth2User: DiscordOAuth2User) {
        if (!existById(discordOAuth2User.getId())) {
            transaction {
                Users.insert {
                    it[id] = discordOAuth2User.getId()
                    it[username] = discordOAuth2User.getUsername()
                    it[avatar] = discordOAuth2User.getAvatar()
                }
            }
        }

    }

    fun updateLoginUser(discordUser: DiscordOAuth2User) {
        transaction {
            UserEntity.findById(discordUser.getId())?.apply {
                username = discordUser.getUsername()
                avatar = discordUser.getAvatar()
            }
        }
    }

    fun findById(userId: Long): UserEntity {
        return transaction {
            UserEntity.findById(userId) ?: throw AlerterNotFoundException(ErrorCode.USER_NOT_FOUND, "")
        }
    }

    fun isLogoutUser(token: String): Boolean {
        return transaction { LogoutToken.isLogoutToken(token) }
    }

    fun getAllUserEntityMap(): Map<Long, UserEntity> {
        return transaction {
            UserEntity.all().map { it.id.value to it }.toMap()
        }
    }

    fun getCurrentUser(): LoginUser {
        val loginUserId = getLoginUserId()
        return transaction {
            UserEntity.findById(loginUserId)?.let { LoginUser.from(it) }
                ?: throw AlerterNotFoundException(ErrorCode.USER_NOT_FOUND, "Login User Not Found")
        }

    }

    fun toggleAlarm() {
        val loginUserId = getLoginUserId()
        transaction {
            UserEntity.findByIdAndUpdate(loginUserId) { it.isAlarm = !it.isAlarm }
        }
    }

    fun updateUserAlarmTime(alarmTimeRequest: AlarmTimeRequest) {
        val loginUserId = getLoginUserId()
        transaction {
            UserEntity.findByIdAndUpdate(loginUserId) { it: UserEntity ->
                it.startTime = alarmTimeRequest.getMinLocalTime()
                it.endTime = alarmTimeRequest.getMaxLocalTime()
            }
        }
    }
}
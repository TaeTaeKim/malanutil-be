package taeyun.malanalter.user

import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import taeyun.malanalter.auth.AlerterUserPrincipal
import taeyun.malanalter.auth.domain.LogoutToken
import taeyun.malanalter.config.exception.*
import taeyun.malanalter.user.domain.UserEntity
import taeyun.malanalter.user.dto.UserRegisterRequest
import java.util.*

@Service
class UserService(
    val passwordEncoder: PasswordEncoder
) {

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

    fun existByUsername(username: String): Boolean {
        return transaction {
            UserEntity.existByUsername(username)
        }
    }

    fun findByUsername(username: String): UserEntity {
        return transaction {
            UserEntity.findByUsername(username) ?: throw AlerterNotFoundException(
                ErrorCode.USER_NOT_FOUND,
                "Can't find user with username $username"
            )
        }
    }

    fun addUser(userRegisterRequest: UserRegisterRequest): UserEntity {
        if (existByUsername(userRegisterRequest.username)) {
            throw AlerterBadRequest(ErrorCode.USER_EXIST, "Username already exists")
        }
        return transaction {
            UserEntity.new {
                username = userRegisterRequest.username
                pwdHash = passwordEncoder.encode(userRegisterRequest.password)
            }
        }
    }

    fun isLogoutUser(token: String): Boolean {
        return transaction { LogoutToken.isLogoutToken(token) }
    }

    fun getAllUserEntityMap() : Map<Long, UserEntity>{
        return transaction {
            UserEntity.all().map { it.userId.value to it }.toMap()
        }
    }
}
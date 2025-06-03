package taeyun.malanalter.user

import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import taeyun.malanalter.auth.domain.LogoutToken
import taeyun.malanalter.config.exception.AlerterBadRequest
import taeyun.malanalter.config.exception.AlerterNotFoundException
import taeyun.malanalter.config.exception.ErrorCode
import taeyun.malanalter.user.domain.UserEntity
import taeyun.malanalter.user.dto.UserRegisterRequest

@Service
class UserService(
    val passwordEncoder: PasswordEncoder
) {

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
}
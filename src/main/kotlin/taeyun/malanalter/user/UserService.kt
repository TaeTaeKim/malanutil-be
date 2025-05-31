package taeyun.malanalter.user

import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import taeyun.malanalter.user.domain.UserEntity
import taeyun.malanalter.user.dto.UserRegisterRequest

@Service
class UserService (
    val passwordEncoder: PasswordEncoder
){

    fun existByUsername(username: String): Boolean {
        return transaction {
            UserEntity.existByUsername(username)
        }
    }

    fun addUser(userRegisterRequest: UserRegisterRequest): UserEntity {
        return transaction {
            UserEntity.new {
                username = userRegisterRequest.username
                pwdHash = passwordEncoder.encode(userRegisterRequest.password)
            }
        }

    }
}
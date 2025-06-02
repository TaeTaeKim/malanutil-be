package taeyun.malanalter.user

import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import taeyun.malanalter.auth.dto.LoginRequest
import taeyun.malanalter.user.domain.UserEntity
import taeyun.malanalter.user.dto.UserRegisterRequest

@Service
class UserService (
    val passwordEncoder: PasswordEncoder = BCryptPasswordEncoder()
){

    fun existByUsername(username: String): Boolean {
        return transaction {
            UserEntity.existByUsername(username)
        }
    }

    fun findByUsername(username: String): UserEntity{
        return transaction {
            UserEntity.findByUsername(username) ?: throw IllegalArgumentException("Can't find user with username $username")
        }
    }

    fun addUser(userRegisterRequest: UserRegisterRequest): UserEntity {
        if( existByUsername(userRegisterRequest.username) ) {
            throw IllegalArgumentException("Username already exists")
        }
        return transaction {
            UserEntity.new {
                username = userRegisterRequest.username
                pwdHash = passwordEncoder.encode(userRegisterRequest.password)
            }
        }
    }

    fun loginUser(loginRequest: LoginRequest) : UserEntity{
        val user = transaction {
            UserEntity.findByUsername(loginRequest.username) ?: throw IllegalArgumentException("User not found")
        }

        if (!passwordEncoder.matches(loginRequest.password, user.pwdHash)) {
            throw IllegalArgumentException("Invalid password")
        }
        return user
    }
}
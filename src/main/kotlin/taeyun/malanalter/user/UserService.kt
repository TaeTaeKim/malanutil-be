package taeyun.malanalter.user

import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Service
import taeyun.malanalter.user.domain.UserEntity
import taeyun.malanalter.user.dto.UserRegisterRequest

@Service
class UserService {

    fun existByUsername(username: String): Boolean {
        return transaction {
            UserEntity.existByUsername(username)
        }
    }

    fun addUser(userRegisterRequest: UserRegisterRequest): UserEntity {
        return transaction {
            UserEntity.new {
                username = userRegisterRequest.username
                pwdHash = userRegisterRequest.password // In a real application, hash the password
            }
        }

    }
}
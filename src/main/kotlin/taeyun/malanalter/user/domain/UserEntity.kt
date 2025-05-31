package taeyun.malanalter.user.domain

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass
import taeyun.malanalter.auth.domain.RefreshToken
import taeyun.malanalter.auth.domain.RefreshTokens


class UserEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<UserEntity>(Users) {

        fun existByUsername(username: String): Boolean {
            return find { Users.username eq username }.count() > 0
        }
    }

    var userId by Users.id
    var username by Users.username
    var pwdHash by Users.pwdHash
    var createdAt by Users.createdAt
    val refreshToken by RefreshToken optionalBackReferencedOn RefreshTokens.userId
}
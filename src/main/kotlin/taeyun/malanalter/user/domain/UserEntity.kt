package taeyun.malanalter.user.domain

import kotlinx.datetime.toJavaLocalTime
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass
import taeyun.malanalter.auth.domain.RefreshToken
import taeyun.malanalter.auth.domain.RefreshTokens
import java.time.LocalTime
import java.time.ZoneId


class UserEntity(id: EntityID<Long>) : LongEntity(id) {

    companion object : LongEntityClass<UserEntity>(Users) {

        fun existByUsername(username: String): Boolean {
            return find { Users.username eq username }.count() > 0
        }

        fun findByUsername(username: String): UserEntity? {
            return find { Users.username eq username }.firstOrNull()
        }
    }

    var userId by Users.id
    var username by Users.username
    var pwdHash by Users.pwdHash
    var createdAt by Users.createdAt
    var discordUrl by Users.discordUrl
    var startTime by Users.startTime
    var endTime by Users.endTime
    val refreshToken by RefreshToken optionalBackReferencedOn RefreshTokens.userId

    fun isAlarmTime(): Boolean {
        val now = LocalTime.now(ZoneId.of("Asia/Seoul"))
        return this.startTime.toJavaLocalTime().isBefore(now) &&
                now.isBefore(this.endTime.toJavaLocalTime())
    }
}
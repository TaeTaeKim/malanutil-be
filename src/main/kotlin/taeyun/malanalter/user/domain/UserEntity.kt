package taeyun.malanalter.user.domain

import kotlinx.datetime.toJavaLocalTime
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass
import java.time.LocalTime
import java.time.ZoneId


class UserEntity(id: EntityID<Long>) : Entity<Long>(id) {

    companion object : EntityClass<Long, UserEntity>(Users)


    var username by Users.username
    var createdAt by Users.createdAt
    var startTime by Users.startTime
    var endTime by Users.endTime
    var disabled by Users.disabled
    var avatar by Users.avatar

    fun isAlarmTime(): Boolean {
        val now = LocalTime.now(ZoneId.of("Asia/Seoul"))
        return this.startTime.toJavaLocalTime().isBefore(now) &&
                now.isBefore(this.endTime.toJavaLocalTime())
    }
}
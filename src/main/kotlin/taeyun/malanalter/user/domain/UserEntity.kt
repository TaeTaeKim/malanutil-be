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
    var isAlarm by Users.isAlarm

    fun isNotAlarmTime(): Boolean {
        if (!isAlarm) {
            return true
        }
        val now = LocalTime.now(ZoneId.of("Asia/Seoul"))
        val start = startTime.toJavaLocalTime()
        val end = endTime.toJavaLocalTime()

        // 유저 알람 시간이 다음날 새벽까지로 설정하는 경우가 있다
        // 퇴근 하는 사람의 경우 19시부터 다음날 2시까지 알람을 울리게 설정할 수 있다.
        // 이 경우는 startTime(19) endTime(2)보다 늦은 시간으로 설정된다.
        // 체크를 반전시켜야한다.
        return if (start.isAfter(end)) {
            // Range spans midnight (e.g., 18:00 - 02:00)
            now.isBefore(start) && now.isAfter(end)
        } else {
            // Normal range (e.g., 09:00 - 17:00)
            now.isBefore(start) || now.isAfter(end)
        }

    }
}
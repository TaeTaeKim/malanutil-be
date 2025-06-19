package taeyun.malanalter.user.domain

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalTime
import kotlinx.datetime.toJavaLocalTime

class UserEntityTest : FunSpec({


    fun notAlarmTime(startTime: LocalTime, endTime: LocalTime, now: java.time.LocalTime): Boolean {
        val start = startTime.toJavaLocalTime()
        val end = endTime.toJavaLocalTime()

        return if (start.isAfter(end)) {
            // Range spans midnight (e.g., 18:00 - 02:00)
            now.isBefore(start) && now.isAfter(end)
        } else {
            // Normal range (e.g., 09:00 - 17:00)
            now.isBefore(start) || now.isAfter(end)
        }
    }
    test("19~3 일 때 현재 시간이 1시이면 알람이 울려야한다.") {
        val startTime = LocalTime(19, 0)
        val endTime = LocalTime(3, 0)
        val now = java.time.LocalTime.of(1, 0)
        notAlarmTime(startTime, endTime, now) shouldBe false
    }

    test("3 ~ 19 일 때 현재 시간이 1시이면 알람이 안 울려야한다.") {
        val startTime = LocalTime(3, 0)
        val endTime = LocalTime(19, 0)
        val now = java.time.LocalTime.of(1, 0)
        notAlarmTime(startTime, endTime, now) shouldBe true
    }

    test("3 ~ 19 일 때 현재 시간이 10시이면 알람이 울려야한다.") {
        val startTime = LocalTime(3, 0)
        val endTime = LocalTime(19, 0)
        val now = java.time.LocalTime.of(10, 0)
        notAlarmTime(startTime, endTime, now) shouldBe false
    }


})



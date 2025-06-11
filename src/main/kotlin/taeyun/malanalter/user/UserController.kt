package taeyun.malanalter.user

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import taeyun.malanalter.user.dto.AlarmTimeRequest
import taeyun.malanalter.user.dto.LoginUser

private val logger = KotlinLogging.logger {  }
@RestController
@RequestMapping("/user")
class UserController(
    val userService: UserService
) {

    @GetMapping
    fun getUserInfo() : LoginUser{
        return userService.getCurrentUser();
    }

    @PatchMapping("/alarm")
    fun updateUserAlarm() {
        return userService.toggleAlarm();
    }

    @PatchMapping("/alarmTime")
    fun updateUserAlarmTime(@RequestBody @Valid alarmTimeRequest: AlarmTimeRequest){
        return userService.updateUserAlarmTime(alarmTimeRequest)
    }
}
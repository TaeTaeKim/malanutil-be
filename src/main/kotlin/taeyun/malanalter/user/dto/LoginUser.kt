package taeyun.malanalter.user.dto

import taeyun.malanalter.user.domain.UserEntity

data class LoginUser(
    val name: String,
    val avatar: String?,
    val isAlarm: Boolean,
    val minTime: Int,
    val maxTime: Int,
    val isAlarmTime: Boolean,

){
    companion object{
        fun from(userEntity: UserEntity) : LoginUser{
            val minMinutes = userEntity.startTime.hour * 60 + userEntity.startTime.minute
            val maxMinutes = userEntity.endTime.hour * 60 + userEntity.endTime.minute

            return LoginUser(
                name = userEntity.username,
                avatar = userEntity.avatar?.takeIf { it.isNotBlank() }?.let {
                    "https://cdn.discordapp.com/avatars/${userEntity.id.value}/${userEntity.avatar}.png"
                },
                isAlarm = userEntity.isAlarm,
                minTime = minMinutes,
                maxTime = maxMinutes,
                isAlarmTime = !userEntity.isNotAlarmTime()
            )
        }
    }
}

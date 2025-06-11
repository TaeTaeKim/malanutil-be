package taeyun.malanalter.user.dto

import taeyun.malanalter.user.domain.UserEntity

data class LoginUser(
    val isAlarm: Boolean,
    val minTime: Int,
    val maxTime: Int
){
    companion object{
        fun from(userEntity: UserEntity) : LoginUser{
            val minMinutes = userEntity.startTime.hour * 60 + userEntity.startTime.minute
            val maxMinutes = userEntity.endTime.hour * 60 + userEntity.endTime.minute

            return LoginUser(
                isAlarm = userEntity.isAlarm,
                minTime = minMinutes,
                maxTime = maxMinutes
            )
        }
    }
}

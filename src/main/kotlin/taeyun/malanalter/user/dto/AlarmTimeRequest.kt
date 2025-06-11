package taeyun.malanalter.user.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import kotlinx.datetime.LocalTime

data class AlarmTimeRequest(
    @field:Max(value = 1440)
    val minTime: Int,
    @field:Min(value = 0)
    val maxTime: Int
){
    fun getMinLocalTime(): LocalTime {
        val hour:Int = minTime / 60
        val min:Int = minTime % 60
        return LocalTime(hour, min)
    }

    fun getMaxLocalTime(): LocalTime {
        if(this.maxTime==1440){
            return LocalTime(23, 59,59)
        }
        val hour:Int = maxTime / 60
        val min:Int = maxTime % 60
        return LocalTime(hour, min)
    }
}


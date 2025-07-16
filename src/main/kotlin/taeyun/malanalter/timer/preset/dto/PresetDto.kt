package taeyun.malanalter.timer.preset.dto

import taeyun.malanalter.timer.preset.domain.PresetEntity

data class PresetDto(
    val presetId:Long,
    val name:String
){
    companion object{
        fun fromEntity(entity: PresetEntity): PresetDto {
            return PresetDto(
                presetId = entity.id.value,
                name = entity.name
            )
        }
    }
}

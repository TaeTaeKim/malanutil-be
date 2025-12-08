package taeyun.malanalter.timer.preset.dto

data class PresetItemDto (
    val itemId: Long,
    val price: Int,
    val isCustom: Boolean,
    val customItemName: String?
){
}
package taeyun.malanalter.timer.preset.dto

import jakarta.validation.constraints.Size

data class PresetItemSaveRequest(
    val itemId: Long,
    val price: Int,
    val isCustom: Boolean,
    @field:Size(max = 10, message = "custom name must not exceed 10 characters.")
    val customItemName: String?
) {
}
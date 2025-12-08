package taeyun.malanalter.timer.preset.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.Size

data class PresetSaveRequest(
    @field:Size(max = 10, message = "preset name must not exceed 10 10 characters")
    val name: String,
    @field:Valid
    val items: List<PresetItemSaveRequest>
) {
}
package taeyun.malanalter.timer.preset.dto

data class PresetSaveRequest(
    val name: String,
    val items: List<PresetItemSaveRequest>
) {
}
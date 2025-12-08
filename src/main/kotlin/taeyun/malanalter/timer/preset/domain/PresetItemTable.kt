package taeyun.malanalter.timer.preset.domain

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable

object PresetItemTable: LongIdTable("preset_item", "preset_item_id") {
    val itemId = long("consume_item_id")
    val price = integer("price")
    val presetId = long("preset_id").references(PresetTable.id, onDelete = ReferenceOption.CASCADE)
    val isCustom = bool("is_custom")
    val customItemName = varchar("custom_name", 32).nullable()
}
package taeyun.malanalter.timer.preset.domain

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass

class PresetItemEntity(id:EntityID<Long>): LongEntity(id) {
    companion object: LongEntityClass<PresetItemEntity>(PresetItemTable)

    var itemId by PresetItemTable.itemId
    var price by PresetItemTable.price
    val isCustom by PresetItemTable.isCustom
    val customItemName by PresetItemTable.customItemName
    var presetId by PresetItemTable.presetId
}
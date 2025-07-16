package taeyun.malanalter.timer.preset.domain

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass

class PresetEntity(id: EntityID<Long>): LongEntity(id) {
    companion object: LongEntityClass<PresetEntity>(PresetTable)

    var name by PresetTable.name
    var userId by PresetTable.userId
    val items by PresetItemEntity referrersOn PresetItemTable.presetId
}
package taeyun.malanalter.schema.domain

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass
import taeyun.malanalter.schema.AlertItemTable

class AlertItemEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object: IntEntityClass<AlertItemEntity>(AlertItemTable) // 간단한 dao 기능 제공?
    var itemId by AlertItemTable.itemId
    var itemCondition by AlertItemTable.itemCondition
    var createdAt by AlertItemTable.createdAt
    val isAlarm by AlertItemTable.isAalarm
}
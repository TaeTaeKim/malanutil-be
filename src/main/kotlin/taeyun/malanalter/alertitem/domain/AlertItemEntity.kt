package taeyun.malanalter.alertitem.domain

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

class AlertItemEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object: IntEntityClass<AlertItemEntity>(AlertItemTable)

    var itemId by AlertItemTable.itemId
    var itemCondition by AlertItemTable.itemCondition
    var createdAt by AlertItemTable.createdAt
    var isAlarm by AlertItemTable.isAalarm
    val userId by AlertItemTable.userId
    val comments by ItemBidEntity referrersOn ItemBidTable.alertItemId orderBy ItemBidTable.price
}
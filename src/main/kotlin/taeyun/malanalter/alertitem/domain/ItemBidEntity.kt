package taeyun.malanalter.alertitem.domain

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass

class ItemBidEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<ItemBidEntity>(ItemBidTable)
    var alertItemId by ItemBidTable.alertItemId
    var url by ItemBidTable.url
    var isAlarm by ItemBidTable.isAlarm
    var comment by ItemBidTable.comment
    var price by ItemBidTable.price
}
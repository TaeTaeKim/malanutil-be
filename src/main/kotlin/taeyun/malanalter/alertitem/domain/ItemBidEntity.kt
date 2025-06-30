package taeyun.malanalter.alertitem.domain

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass

class ItemBidEntity(id: EntityID<String>) : Entity<String>(id) {
    companion object : EntityClass<String, ItemBidEntity>(ItemBidTable)
    var alertItemId by ItemBidTable.alertItemId
    var isAlarm by ItemBidTable.isAlarm
    var comment by ItemBidTable.comment
    var price by ItemBidTable.price
    var alertItem by AlertItemEntity referencedOn ItemBidTable.alertItemId
}
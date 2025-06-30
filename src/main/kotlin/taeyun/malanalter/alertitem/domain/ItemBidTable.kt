package taeyun.malanalter.alertitem.domain

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IdTable

object ItemBidTable: IdTable<String>("item_bid") {
    override val id : Column<EntityID<String>> = varchar(name="alert_url",255).entityId()
    val alertItemId = integer("alert_item_id").references(AlertItemTable.id, onDelete = ReferenceOption.CASCADE)
    val price = long("price")
    val comment = varchar("comment", 255).nullable()
    val isAlarm = bool("is_alarm")

    override val primaryKey = PrimaryKey(id)
}
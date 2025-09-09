package taeyun.malanalter.alertitem.domain

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable

object ItemBidTable: LongIdTable("item_bid") {
    val alertItemId = reference("alert_item_id", AlertItemTable.id, onDelete = ReferenceOption.CASCADE)
    val url = varchar("bid_url", 255).index(isUnique = false)
    val price = long("price")
    val comment = varchar("comment", 255).nullable()
    val isSent = bool("is_sent").default(false)
    val isAlarm = bool("is_alarm").default(true)

}
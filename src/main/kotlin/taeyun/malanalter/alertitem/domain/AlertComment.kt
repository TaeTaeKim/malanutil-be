package taeyun.malanalter.alertitem.domain

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass

class AlertComment(id: EntityID<String>) : Entity<String>(id) {
    companion object : EntityClass<String, AlertComment>(AlertComments)

    var itemId by AlertComments.itemId
    var isAlarm by AlertComments.isAlarm
    var alertItem by AlertItemEntity referencedOn AlertComments.itemId
}
package taeyun.malanalter.alertitem.domain

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass

class AlertComment(id: EntityID<String>) : Entity<String>(id) {
    companion object : EntityClass<String, AlertComment>(AlertComments)

    val alertItemId by AlertComments.alertItemId
    var isAlarm by AlertComments.isAlarm
    val comment by AlertComments.comment
    val alertItem by AlertItemEntity referencedOn AlertComments.alertItemId
}
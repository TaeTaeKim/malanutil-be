package taeyun.malanalter.alertitem.domain

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IdTable

object AlertComments: IdTable<String>("disabled_comment") {
    override val id : Column<EntityID<String>> = varchar(name="alert_url",255).entityId()
    val itemId = integer("item_id").references(AlertItemTable.id, onDelete = ReferenceOption.CASCADE)
    val isAlarm = bool("is_alarm")

    override val primaryKey = PrimaryKey(id)
}
package taeyun.malanalter.user.domain

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.core.dao.id.ULongIdTable
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime
import org.jetbrains.exposed.v1.datetime.time

object Users : IdTable<Long>() {
    override val id: Column<EntityID<Long>> = long("user_id").entityId()
    val username = varchar("username", 50)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    var startTime = time("start_time")
    var endTime = time("end_time")
    var disabled = bool("disabled").default(false)

    override val primaryKey = PrimaryKey(id)
}
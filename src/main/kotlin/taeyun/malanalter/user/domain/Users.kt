package taeyun.malanalter.user.domain

import kotlinx.datetime.LocalTime
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime
import org.jetbrains.exposed.v1.datetime.time

object Users : IdTable<Long>() {
    override val id: Column<EntityID<Long>> = long("user_id").entityId()
    val username = varchar("username", 50)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    var startTime = time("start_time").default(LocalTime.parse("00:00:00"))
    var endTime = time("end_time").default(LocalTime.parse("23:59:59"))
    var disabled = bool("disabled").default(false)
    var avatar = text(name = "avatar").nullable()
    var isAlarm = bool("is_alarm").default(true)

    override val primaryKey = PrimaryKey(id)
}
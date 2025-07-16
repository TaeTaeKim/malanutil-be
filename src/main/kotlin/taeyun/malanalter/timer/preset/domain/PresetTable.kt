package taeyun.malanalter.timer.preset.domain

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime
import taeyun.malanalter.user.domain.Users

object PresetTable: LongIdTable("preset", columnName = "preset_id"){
    val name = varchar("preset_name", 255)
    val userId = long("user_id").references(Users.id, onDelete = ReferenceOption.CASCADE)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
}
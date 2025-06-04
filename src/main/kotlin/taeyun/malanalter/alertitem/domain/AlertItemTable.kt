package taeyun.malanalter.alertitem.domain

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime
import org.jetbrains.exposed.v1.json.jsonb
import taeyun.malanalter.alertitem.dto.ItemCondition
import taeyun.malanalter.user.domain.Users

val mapper = jacksonObjectMapper()

object AlertItemTable : IntIdTable("alert_item") {
    val itemId = integer("item_id")
    val itemCondition = jsonb(
        "item_condition",
        { mapper.writeValueAsString(it)},
        { mapper.readValue(it, ItemCondition::class.java) }
    )
    val isAalarm = bool("is_alarm").default(true)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val userId = long("user_id").references(Users.id, onDelete = ReferenceOption.CASCADE)
}
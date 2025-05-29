package taeyun.malanalter.schema

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime
import org.jetbrains.exposed.v1.json.jsonb
import taeyun.malanalter.dto.ItemCondition

val mapper = jacksonObjectMapper()

object AlertItemTable : IntIdTable("alert_item") {
    val itemId = integer("item_id").uniqueIndex()
    val itemCondition = jsonb(
        "item_condition",
        {mapper.writeValueAsString(it)},
        {mapper.readValue(it, ItemCondition::class.java) }
    )
    val isAalarm = bool("is_alarm").default(true)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
}
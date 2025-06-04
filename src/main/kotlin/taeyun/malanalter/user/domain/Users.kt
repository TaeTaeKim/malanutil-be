package taeyun.malanalter.user.domain

import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime
import org.jetbrains.exposed.v1.datetime.time

object Users : LongIdTable(columnName = "user_id") {
    val username = varchar("username", 50).uniqueIndex()
    val pwdHash = varchar("pwd_hash", 255)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    var discordUrl = text("discord_url").nullable()
    var startTime = time("start_time")
    var endTime = time("end_time")
}
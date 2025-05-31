package taeyun.malanalter.user.domain

import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime

object Users : LongIdTable(columnName = "user_id") {
    val username = varchar("username", 50).uniqueIndex()
    val pwdHash = varchar("pwd_hash", 255)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
}
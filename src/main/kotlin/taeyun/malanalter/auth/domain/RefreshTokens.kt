package taeyun.malanalter.auth.domain

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime
import taeyun.malanalter.user.domain.Users

object RefreshTokens : LongIdTable(columnName = "token_id") {
    val userId = long("user_id").references(Users.id, onDelete = ReferenceOption.CASCADE)
    val token = varchar("token", 255).uniqueIndex()
    val expiredAt = datetime("expired_at")
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val isRevoked = bool("is_revoked").default(false)
}
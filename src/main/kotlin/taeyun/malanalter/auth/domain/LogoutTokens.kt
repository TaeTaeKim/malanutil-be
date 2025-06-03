package taeyun.malanalter.auth.domain

import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime

object LogoutTokens : LongIdTable(){
    val logoutToken = varchar("logout_token", 255).index()
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
}
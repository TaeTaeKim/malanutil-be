package taeyun.malanalter.auth.domain

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass

class LogoutToken(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<LogoutToken>(LogoutTokens) {
        fun isLogoutToken(token: String): Boolean {
            return find { LogoutTokens.logoutToken eq token }.count() > 0
        }
    }

    var logoutToken by LogoutTokens.logoutToken
    var createdAt by LogoutTokens.createdAt
}
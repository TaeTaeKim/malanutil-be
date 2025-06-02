package taeyun.malanalter.auth.domain

import kotlinx.datetime.toJavaLocalDateTime
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass

class RefreshToken(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<RefreshToken>(RefreshTokens) {
        fun findRefreshTokenByUserId(userId: Long, oldRefreshToken: String): RefreshToken? {
                return find { (RefreshTokens.userId eq userId) and (RefreshTokens.token eq oldRefreshToken) }.firstOrNull()
        }
    }

    var tokenId by RefreshTokens.id
    var userId by RefreshTokens.userId
    var token by RefreshTokens.token
    var expiredAt by RefreshTokens.expiredAt
    var createdAt by RefreshTokens.createdAt
    var isRevoked by RefreshTokens.isRevoked


    fun isExpired(): Boolean {
        return this.expiredAt.toJavaLocalDateTime().isBefore(java.time.LocalDateTime.now())
    }
}
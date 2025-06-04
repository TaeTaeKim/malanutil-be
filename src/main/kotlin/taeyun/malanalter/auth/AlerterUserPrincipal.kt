package taeyun.malanalter.auth

import org.springframework.security.core.GrantedAuthority
import taeyun.malanalter.user.domain.UserEntity
import java.security.Principal

data class AlerterUserPrincipal(
    val userId: Long,
    val username: String,
    val authorities: Collection<GrantedAuthority> = emptyList()
) : Principal {
    override fun getName(): String = username

    companion object {
        fun of(userEntity: UserEntity):AlerterUserPrincipal{
            return AlerterUserPrincipal(userEntity.userId.value, userEntity.username)
        }
    }
}

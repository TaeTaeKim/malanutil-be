package taeyun.malanalter.auth.discord

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.OAuth2User
import taeyun.malanalter.config.exception.AlerterBadRequest
import taeyun.malanalter.config.exception.ErrorCode

class DiscordOAuth2User(
    private val oauth2User: OAuth2User,
    private val userRequest: OAuth2UserRequest
) : OAuth2User{

    private var token: String = userRequest.accessToken.tokenValue?: throw AlerterBadRequest(ErrorCode.DISCORD_TOKEN_NOTFOUND, null)

    override fun getName(): String = getAttribute("id")!!

    override fun getAttributes(): Map<String, Any> = oauth2User.attributes

    override fun getAuthorities(): Collection<GrantedAuthority> = oauth2User.authorities

    // 이 부분의 attribute 내용이 모두 다 달라서 OAuth2UserInfo 로 추상화 했다.
    fun getId(): Long = java.lang.Long.parseLong(this.name)

    fun getEmail(): String? = getAttribute("email")

    fun getGlobalName():String? = getAttribute<String>("global_name")

    fun getUsername():String = this.getGlobalName()?:getAttribute("username")!!

    fun getAvatar():String? = getAttribute("avatar")

    fun getToken(): String = token



}
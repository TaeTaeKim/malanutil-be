package taeyun.malanalter.auth.discord

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import taeyun.malanalter.auth.JwtUtil
import taeyun.malanalter.auth.domain.RefreshToken
import taeyun.malanalter.user.UserService

@Component
class OAuth2SuccessHandler(
    val userService: UserService,
    val discordService: DiscordService,
    val jwtUtil: JwtUtil,
    val environment: Environment
) : SimpleUrlAuthenticationSuccessHandler() {
    override fun onAuthenticationSuccess(
        request: HttpServletRequest?,
        response: HttpServletResponse?,
        authentication: Authentication?
    ) {

        // 로그인 유저를 user table에 등록
        val discordOAuth2User = authentication?.principal as DiscordOAuth2User
        userService.addUser(discordOAuth2User)
        discordService.addUserToServer(discordOAuth2User)
        discordService.sendDirectMessage(discordOAuth2User.getId(), "웰컴인사")
        val generateAccessToken = jwtUtil.generateAccessToken(discordOAuth2User.getId())
        val generateRefreshToken = jwtUtil.generateRefreshToken()

        response!!.sendRedirect(getLoginCallBackUrl(generateAccessToken, generateRefreshToken))
    }

    fun getLoginCallBackUrl(accessToken: String, refreshToken: String): String {
        val authCallback = if (environment.acceptsProfiles(Profiles.of("dev"))) {
            "/alerter/auth/callback"
        } else {
            "/auth/callback"
        }
        return "$authCallback?access_token=$accessToken&refresh_token=$refreshToken"


    }


}
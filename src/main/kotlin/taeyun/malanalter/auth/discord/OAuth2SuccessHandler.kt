package taeyun.malanalter.auth.discord

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import taeyun.malanalter.alertitem.dto.DiscordMessageContainer
import taeyun.malanalter.auth.AlerterCookieOauth2AuthRequestRepository
import taeyun.malanalter.auth.AlerterCookieUtil
import taeyun.malanalter.auth.AlerterCookieUtil.Companion.OAUTH2_REDIRECT_SOURCE_COOKIE_NAME
import taeyun.malanalter.auth.AuthService
import taeyun.malanalter.auth.JwtUtil
import taeyun.malanalter.config.property.FrontEndProperties
import taeyun.malanalter.user.UserService

@Component
class OAuth2SuccessHandler(
    val userService: UserService,
    val discordService: DiscordService,
    val authService: AuthService,
    val jwtUtil: JwtUtil,
    val oauth2Repository: AlerterCookieOauth2AuthRequestRepository,
    val frontEndProperties: FrontEndProperties
) : SimpleUrlAuthenticationSuccessHandler() {
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val baseTargetUrl = determineTargetUrl(request, response, authentication)

        if (response.isCommitted) {
            logger.debug("Response has already been committed. Unable to redirect to $baseTargetUrl")
            return
        }

        val discordOAuth2User = authentication.principal as DiscordOAuth2User
        val accessToken = jwtUtil.generateAccessToken(discordOAuth2User.getId())
        val refreshToken = jwtUtil.generateRefreshToken()
        discordService.addUserToServer(discordOAuth2User)
        if (userService.existById(discordOAuth2User.getId())) {
            userService.updateLoginUser(discordOAuth2User)
        } else {
            userService.addLoginUser(discordOAuth2User)
            discordService.sendDirectMessage(discordOAuth2User.getId(), DiscordMessageContainer.welcomeMessage())
        }
        authService.registerRefreshToken(discordOAuth2User.getId(), refreshToken)
        response.addCookie(AlerterCookieUtil.makeRefreshTokenCookie(refreshToken))

        // Build the final redirect URL with the access token
        val finalTargetUrl = UriComponentsBuilder.fromUriString(baseTargetUrl)
            .queryParam("accessToken", accessToken)
            .build().toUriString()

        clearAuthenticationAttributes(request, response)
        redirectStrategy.sendRedirect(request, response, finalTargetUrl)
    }

    override fun determineTargetUrl(request: HttpServletRequest, response: HttpServletResponse, authentication: Authentication): String {
        val source = AlerterCookieUtil.getCookie(request, OAUTH2_REDIRECT_SOURCE_COOKIE_NAME)?.value

        val callbackUri = when (source) {
            "timer" -> frontEndProperties.timerCallbackUrl
            "alerter" -> frontEndProperties.alerterCallbackUrl
            "pat" -> frontEndProperties.patCallbackUrl
            else -> frontEndProperties.alerterCallbackUrl
        }
        return UriComponentsBuilder.fromUriString(frontEndProperties.redirectionUrl).path(callbackUri).build().toUriString()
    }

    protected fun clearAuthenticationAttributes(request: HttpServletRequest, response: HttpServletResponse) {
        super.clearAuthenticationAttributes(request)
        oauth2Repository.removeAuthorizationRequestCookies(request, response)
    }
}
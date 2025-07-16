package taeyun.malanalter.auth

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.stereotype.Component
import taeyun.malanalter.auth.AlerterCookieUtil.Companion.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME
import taeyun.malanalter.auth.AlerterCookieUtil.Companion.OAUTH2_REDIRECT_SOURCE_COOKIE_NAME

@Component
class AlerterCookieOauth2AuthRequestRepository : AuthorizationRequestRepository<OAuth2AuthorizationRequest> {
    override fun loadAuthorizationRequest(request: HttpServletRequest): OAuth2AuthorizationRequest? {
        return AlerterCookieUtil.getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
            ?.let { AlerterCookieUtil.deserialize(it, OAuth2AuthorizationRequest::class.java) }
    }

    override fun saveAuthorizationRequest(
        authorizationRequest: OAuth2AuthorizationRequest?,
        request: HttpServletRequest,
        response: HttpServletResponse
    ) {
        if (authorizationRequest == null) {
            AlerterCookieUtil.deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
            AlerterCookieUtil.deleteCookie(request, response, OAUTH2_REDIRECT_SOURCE_COOKIE_NAME)
            return
        }
        // Security OAuth2 에 필요한 AuthRequest 저장
        AlerterCookieUtil.addCookie(
            response,
            OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME,
            AlerterCookieUtil.serialize(authorizationRequest),
            AlerterCookieUtil.cookieExpireSeconds
        )

        // CallBack URI 를 위한 Custom Cookie 저장
        val redirectSource = request.getParameter("source") ?: "alerter"
        if (redirectSource.isNotBlank()) {
            AlerterCookieUtil.addCookie(
                response,
                OAUTH2_REDIRECT_SOURCE_COOKIE_NAME,
                redirectSource,
                AlerterCookieUtil.cookieExpireSeconds
            )
        }
    }

    override fun removeAuthorizationRequest(
        request: HttpServletRequest,
        response: HttpServletResponse
    ): OAuth2AuthorizationRequest? {
        return this.loadAuthorizationRequest(request)
    }

    fun removeAuthorizationRequestCookies(request: HttpServletRequest, response: HttpServletResponse) {
        AlerterCookieUtil.deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
        AlerterCookieUtil.deleteCookie(request, response, OAUTH2_REDIRECT_SOURCE_COOKIE_NAME)
    }
}
package taeyun.malanalter.auth

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.commons.lang3.SerializationUtils
import java.util.*

class AlerterCookieUtil {
    companion object {
        const val REFRESH_TOKEN_NAME = "refreshToken"
        const val OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request"
        const val OAUTH2_REDIRECT_SOURCE_COOKIE_NAME = "redirect_source"
        const val cookieExpireSeconds = 180
        fun makeRefreshTokenCookie(refreshToken: String): Cookie = Cookie(REFRESH_TOKEN_NAME, refreshToken)
            .apply {
                isHttpOnly = true
                maxAge = 60 * 60 * 24 * 7 // 7 days
                path = "/"
            }

        fun getCookie(request: HttpServletRequest, name: String): Cookie? {
            val cookies = request.cookies
            if (cookies != null && cookies.isNotEmpty()) {
                for (cookie in cookies) {
                    if (cookie.name == name) {
                        return cookie
                    }
                }
            }
            return null
        }

        fun addCookie(response: HttpServletResponse, name: String, value: String, maxAge: Int) {
            val cookie = Cookie(name, value)
            cookie.path = "/"
            cookie.isHttpOnly = true
            cookie.maxAge = maxAge
            response.addCookie(cookie)
        }

        fun deleteCookie(request: HttpServletRequest, response: HttpServletResponse, name: String) {
            val cookies = request.cookies
            if (cookies != null && cookies.isNotEmpty()) {
                for (cookie in cookies) {
                    if (cookie.name == name) {
                        cookie.value = ""
                        cookie.path = "/"
                        cookie.maxAge = 0
                        response.addCookie(cookie)
                    }
                }
            }
        }

        fun serialize(obj: Any): String {
            return Base64.getUrlEncoder().encodeToString(SerializationUtils.serialize(obj as java.io.Serializable))
        }

        fun <T> deserialize(cookie: Cookie, cls: Class<T>): T {
            return cls.cast(SerializationUtils.deserialize(Base64.getUrlDecoder().decode(cookie.value)))
        }
    }
}
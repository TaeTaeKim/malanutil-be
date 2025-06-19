package taeyun.malanalter.auth

import jakarta.servlet.http.Cookie

class AlerterCookieUtil {
    companion object {
        const val REFRESH_TOKEN_NAME = "refreshToken"
        fun makeRefreshTokenCookie(refreshToken: String): Cookie = Cookie(REFRESH_TOKEN_NAME, refreshToken)
            .apply {
                isHttpOnly = true
                maxAge = 60 * 60 * 24 * 7 // 7 days
                path = "/"
            }
    }
}
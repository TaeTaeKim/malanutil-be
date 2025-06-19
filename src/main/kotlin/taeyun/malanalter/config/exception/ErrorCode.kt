package taeyun.malanalter.config.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val status: HttpStatus,
    val code: String,
    val defaultMessage: String // if exception's message not defined
) {
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_001", "Invalid Token"),
    // AUTH_002, AUTH_003 은 무조건 전부 /login 으로 이동한다.
    EXPIRED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_002", "Expired AccessToken : refresh required"),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "AUTH_002", "Refresh Token not found"),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_002", "Expired Refresh Token RE-login required"),
    LOGOUT_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_002", "Logout Account"),
    DISCORD_TOKEN_NOTFOUND(HttpStatus.BAD_REQUEST, "AUTH_006", "No discord access token found"),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "User Not Found"),

    BAD_REQUEST(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "잘못된 요청입니다."),

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_001", "Internal Server Error"),
}
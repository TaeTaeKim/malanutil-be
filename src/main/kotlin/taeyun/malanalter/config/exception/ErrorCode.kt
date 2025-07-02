package taeyun.malanalter.config.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val status: HttpStatus,
    val code: String,
    val defaultMessage: String // if exception's message not defined
) {
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_001", "Invalid Token"),
    // AUTH_002 는 리프레시 요청
    EXPIRED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_002", "Expired AccessToken : refresh required"),

    // AUTH_003 은 로그인으로 푸시한다(FE).
    LOGOUT_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_003", "Logout Account"),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "AUTH_003", "Refresh Token not found"),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_003", "Expired Refresh Token RE-login required"),

    DISCORD_TOKEN_NOTFOUND(HttpStatus.BAD_REQUEST, "AUTH_006", "No discord access token found"),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "User Not Found"),

    // 400
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "허가되지 않는 요청입니다."),

    // 500
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_001", "Internal Server Error"),
}
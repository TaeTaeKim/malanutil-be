package taeyun.malanalter.config.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val status: HttpStatus,
    val code: String,
    val defaultMessage: String // if exception's message not defined
) {
    // AUTH_002, AUTH_003 은 무조건 전부 /login 으로 이동한다.
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_001", "Invalid Token"),
    EXPIRED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_002", "Expired AccessToken : refresh required"),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_003", "Expired Refresh Token RE-login required"),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "AUTH_004", "Refresh Token not found"),

    // Login
    WRONG_PASSWORD(HttpStatus.NOT_ACCEPTABLE, "AUTH_005", "WRONG_PASSWORD"),
    LOGOUT_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_005", "Logout Account"),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "User Not Found"),
    USER_EXIST(HttpStatus.BAD_REQUEST, "USER_002", "User Already Exist"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_001", "Internal Server Error")
}
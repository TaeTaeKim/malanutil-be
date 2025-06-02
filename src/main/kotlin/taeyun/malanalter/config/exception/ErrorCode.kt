package taeyun.malanalter.config.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val status: HttpStatus,
    val code: String,
    val defaultMessage: String // if exception's message not defined
) {
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_001", "Invalid Token"),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_002", "Expired Token"),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "AUTH_003", "Refresh Token not found"),


    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_001", "Internal Server Error")
}
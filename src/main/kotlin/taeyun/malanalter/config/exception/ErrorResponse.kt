package taeyun.malanalter.config.exception

import org.springframework.http.HttpStatus
import java.time.LocalDateTime

data class ErrorResponse(
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val status: HttpStatus,
    val code: String,
    val message: String
) {
    companion object {
        fun of(exception: BaseException) = ErrorResponse(
            status = exception.errorCode.status,
            code = exception.errorCode.code,
            message = exception.getErrorMessage()
        )

        fun of(errorCode: ErrorCode) = ErrorResponse(
            status = errorCode.status,
            code = errorCode.code,
            message = errorCode.defaultMessage
        )
    }

}
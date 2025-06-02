package taeyun.malanalter.config.exception

sealed class BaseException(
    val errorCode: ErrorCode,
    override val message: String = errorCode.defaultMessage,
    override val cause: Throwable
) : RuntimeException(message, cause) {

    fun getErrorMessage(): String {
        return cause.message ?: errorCode.defaultMessage
    }
}

class InvalidTokenException(
    errorCode: ErrorCode = ErrorCode.INVALID_TOKEN,
    cause: Throwable
) : BaseException(errorCode, cause.message ?: errorCode.defaultMessage, cause)
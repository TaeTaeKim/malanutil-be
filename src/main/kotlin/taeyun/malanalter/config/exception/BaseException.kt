package taeyun.malanalter.config.exception

sealed class BaseException(
    val errorCode: ErrorCode,
    override val message: String? = errorCode.defaultMessage, // 하위 클래스에서 Null 을 주면 default 메세지 들어간다.
) : RuntimeException(message)


class AlerterJwtException(
    errorCode: ErrorCode,
    message: String?
) : BaseException(errorCode, message)

class AlerterNotFoundException(
    errorCode: ErrorCode,
    message: String? = null
) : BaseException(errorCode, message)

class AlerterServerError(
    errorCode: ErrorCode = ErrorCode.INTERNAL_SERVER_ERROR,
    message: String? = null
) : BaseException(errorCode, message)

class AlerterBadRequest(
    errorCode: ErrorCode,
    message: String?
) : BaseException(errorCode, message)
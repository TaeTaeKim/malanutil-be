package taeyun.malanalter.config.exception

sealed class BaseException(
    val errorCode: ErrorCode,
    override val message: String? = errorCode.defaultMessage,// 하위 클래스에서 Null 을 주면 default 메세지 들어간다.
    val isAlarm: Boolean? = false,
) : RuntimeException(message)


class AlerterJwtException(
    errorCode: ErrorCode,
    message: String?,
    isAlarm: Boolean=false
) : BaseException(errorCode, message, isAlarm)

class AlerterNotFoundException(
    errorCode: ErrorCode,
    message: String? = null,
    isAlarm: Boolean=false
) : BaseException(errorCode, message ,isAlarm)

class AlerterServerError(
    errorCode: ErrorCode = ErrorCode.INTERNAL_SERVER_ERROR,
    val uuid: String,
    message: String,
    val rootCause: Exception?,
) : BaseException(errorCode, rootCause?.message ?: "[$uuid] $message", isAlarm = true)

class AlerterBadRequest(
    errorCode: ErrorCode,
    message: String?,
    isAlarm: Boolean=false
) : BaseException(errorCode, message, isAlarm)

class PartyBadRequest(
    errorCode: ErrorCode,
    message: String?
) : BaseException(errorCode, message)

class PartyServerError(
    errorCode: ErrorCode = ErrorCode.INTERNAL_SERVER_ERROR,
    val uuid: String,
    message: String,
    val rootCause: Exception?,
) : BaseException(errorCode, rootCause?.message ?: "[$uuid] $message", isAlarm = true)
package taeyun.malanalter.config.exception

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.reactive.function.client.WebClient


val logger = KotlinLogging.logger{}

@RestControllerAdvice
class GlobalControllerAdvice(@Qualifier(value = "discordClient") val discordClient: WebClient) {

    @ExceptionHandler(BaseException::class)
    fun handleBaseException(ex: BaseException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        // 서버에러의 경우 추적할 수 있는 uuid 와 함께 root cause 와 함께
        if (ex is AlerterServerError) {
            logger.error { "[${ex.uuid}] Server Error occur : ${ex.rootCause?.message ?: ex.message}\n 스택 트레이스 ${ex.rootCause?.printStackTrace()}" }
        }
        if (ex.isAlarm == true) {
            discordClient.post().bodyValue(ErrorNotification.fromException(ex)).retrieve().toBodilessEntity()
                .block()
        }
        logger.warn { "[${ex.errorCode.status}]  ${ex.message}" }
        val errorResponse = ErrorResponse.of(ex)
        return ResponseEntity.status(errorResponse.status).body(errorResponse)
    }

    @ExceptionHandler(RuntimeException::class)
    fun handleServerError(ex: RuntimeException): ResponseEntity<ErrorResponse> {
        logger.error { "Internal Server Error with ${ex.message} \n\n ${ex.printStackTrace()}" }
        discordClient.post().bodyValue(ErrorNotification.fromException(ex)).retrieve()
        val errorResponse = ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }

    @ExceptionHandler(BindException::class)
    fun handleBindError(e: BindException): ResponseEntity<ErrorResponse>{
        val errormsg = e.bindingResult.allErrors[0].defaultMessage
        val errorResponse = ErrorResponse.of(AlerterBadRequest(ErrorCode.BAD_REQUEST, message = errormsg, true))
        return ResponseEntity.status(errorResponse.status).body(errorResponse)
    }

}
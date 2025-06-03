package taeyun.malanalter.auth

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import taeyun.malanalter.config.exception.BaseException
import taeyun.malanalter.config.exception.ErrorCode
import taeyun.malanalter.config.exception.ErrorResponse
import java.util.UUID
import kotlin.math.log

private val logger = KotlinLogging.logger {  }
@Component
class JwtAuthExceptionFilter(private val jacksonObjectMapper: ObjectMapper) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            filterChain.doFilter(request, response)
        }catch (alerterException: BaseException){
            setResponseWithBaseException(response, alerterException)
        }catch (exception : Exception){
            setResponseWithException(response, exception)
        }
    }

    fun setResponseWithBaseException(response: HttpServletResponse, exception: BaseException) {
        val objectMapper = jacksonObjectMapper().registerModules(JavaTimeModule())
        val errorBody = ErrorResponse.of(exception)
        response.status = exception.errorCode.status.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"
        response.writer.write(objectMapper.writeValueAsString(errorBody))
    }

    fun setResponseWithException(response: HttpServletResponse, exception: Exception){
        val objectMapper = jacksonObjectMapper().registerModules(JavaTimeModule())
        val randomUUID = UUID.randomUUID()
        logger.error("$randomUUID ${exception.message}")
        val errorResponse = ErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR,
            code = ErrorCode.INTERNAL_SERVER_ERROR.code,
            message = randomUUID.toString()
        )
        response.status = HttpStatus.INTERNAL_SERVER_ERROR.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"
        response.writer.write(objectMapper.writeValueAsString(errorResponse))

    }

}
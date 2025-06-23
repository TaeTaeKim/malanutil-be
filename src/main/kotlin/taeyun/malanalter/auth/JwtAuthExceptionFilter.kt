package taeyun.malanalter.auth

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import taeyun.malanalter.config.exception.AlerterJwtException
import taeyun.malanalter.config.exception.BaseException
import taeyun.malanalter.config.exception.ErrorResponse

private val log = KotlinLogging.logger {  }
@Component
class JwtAuthExceptionFilter() : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            filterChain.doFilter(request, response)
        } catch (jwtException: AlerterJwtException) {
            setResponseWithBaseException(response, jwtException)
        }
    }

    fun setResponseWithBaseException(response: HttpServletResponse, exception: BaseException) {
        val objectMapper = jacksonObjectMapper().registerModules(JavaTimeModule())
        val errorBody = ErrorResponse.of(exception)
        log.warn{"JWT 인증 중 의도된 예외발생 : ${exception.message} "}
        response.status = exception.errorCode.status.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"
        response.writer.write(objectMapper.writeValueAsString(errorBody))
    }
}
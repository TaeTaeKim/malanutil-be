package taeyun.malanalter.auth

import io.github.oshai.kotlinlogging.KotlinLogging
import io.jsonwebtoken.JwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import taeyun.malanalter.config.SecurityConfig
import taeyun.malanalter.config.exception.*
import taeyun.malanalter.user.UserService
import java.util.*


private val logger = KotlinLogging.logger {}

@Component
class JwtAuthenticationFilter(
    val jwtUtil: JwtUtil,
    val userService: UserService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // 인증이 필요없는 요청에 대해서는 filter pass
        val openedUrlMatcher = SecurityConfig.getOpenUrlMatchers()
        if (openedUrlMatcher.any { it.matches(request) }) {
            logger.info("skip check jwt for url ${request.requestURI}")
            filterChain.doFilter(request, response)
            return
        }
        // request header 의 Authorization header 검증 및 토큰 추출
        val authHeader = request.getHeader("Authorization")
        if (authHeader == null || !authHeader.startsWith("Bearer")) {
            logger.info("No Auth Requested From Host :  ${request.requestURL}")
            throw AlerterJwtException(ErrorCode.INVALID_TOKEN, "No Auth header in request")
        }
        // 검증로직
        val jwt = authHeader.substring(7)
        try {
            if (jwtUtil.isExpiredToken(jwt)) {
                logger.info { "Expired Token with $jwt" }
                throw AlerterJwtException(ErrorCode.EXPIRED_ACCESS_TOKEN, "Access Token expired")
            }
            val username = jwtUtil.getUsername(jwt)
            // 없는 사용자라면 exception 배출
            if (!validUser(username)) {
                throw AlerterNotFoundException(ErrorCode.USER_NOT_FOUND, "User $username not found")
            }
            if (SecurityContextHolder.getContext().authentication == null) {
                val authToken = UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    emptyList() // some additional role authorities
                )
                authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                SecurityContextHolder.getContext().authentication = authToken
            }
        } catch (e: BaseException) {
            throw e
        } catch (e: JwtException) { // jwt
            val uuid = UUID.randomUUID().toString()
            logger.info("[$uuid]Error in Handling Token $jwt", e)
            throw AlerterJwtException(
                ErrorCode.INVALID_TOKEN,
                "[UUID : $uuid] Error in checking JWT token See server log"
            )
        } catch (e: Exception) {
            val uuid = UUID.randomUUID().toString()
            logger.error("Unexpected Error occur when validate user token", e)
            throw AlerterServerError(message = "[UUID : $uuid] Unexpected Error occur when validate user token")
        }
        filterChain.doFilter(request, response)

    }

    private fun validUser(username: String): Boolean {
        return username.isNotBlank() && userService.existByUsername(username)

    }

}
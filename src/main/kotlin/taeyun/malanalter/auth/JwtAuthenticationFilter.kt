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


private val log = KotlinLogging.logger {}

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
        // 인증이 필요없는 요청에 대해서는 filter pass -> Security Context 로 id가 들어가지 않는다.
        if (SecurityConfig.getOpenUrlMatchers().any { it.matches(request) }) {
            log.debug{"skip check jwt for url ${request.requestURI}"}
            filterChain.doFilter(request, response)
            return
        }
        // request header 의 Authorization header 검증 및 토큰 추출
        val jwt = JwtUtil.getTokenFromRequest(request)

        // 검증로직
        try {
            val userId = jwtUtil.getUserFromExpiredToken(jwt)
            // 로그아웃된 토큰 검사
            if (userService.isLogoutUser(jwt)) {
                throw AlerterJwtException(ErrorCode.LOGOUT_TOKEN, "로그아웃된 토큰 요청 : 유저 $userId, Token: $jwt")
            }
            // 만료검사 : 만료시에 바로 Exception 이 발생
            if (jwtUtil.isExpiredToken(jwt)) {
                throw AlerterJwtException(ErrorCode.EXPIRED_ACCESS_TOKEN, "만료된 액세스 토큰 요청")
            }

            // 없는 사용자라면 exception 배출
            if (!validUser(userId)) {
                throw AlerterJwtException(ErrorCode.USER_NOT_FOUND, "User $userId not found")
            }
            if (SecurityContextHolder.getContext().authentication == null) {
                val findUserEntity = userService.findById(userId)
                val authToken = UsernamePasswordAuthenticationToken(
                    AlerterUserPrincipal.of(findUserEntity),
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
            log.error{"[$uuid]Error in Handling Token $jwt ${e.printStackTrace()}"}
            throw AlerterJwtException(
                ErrorCode.INVALID_TOKEN,
                "[UUID : $uuid] Error in checking JWT token See server log"
            )
        } catch (e: Exception) {
            val uuid = UUID.randomUUID().toString()
            log.error{"[$uuid] Unexpected Error occur when validate user token $jwt ${e.printStackTrace()}"}
            throw AlerterServerError(
                uuid = uuid,
                message = "[$uuid] Unexpected Error occur when validate user token See Sever log",
                rootCause = e
            )
        }
        filterChain.doFilter(request, response)

    }

    private fun validUser(userId: Long): Boolean {
        return userService.existById(userId)

    }

}
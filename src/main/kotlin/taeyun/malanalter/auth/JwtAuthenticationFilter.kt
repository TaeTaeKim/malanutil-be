package taeyun.malanalter.auth

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import taeyun.malanalter.config.SecurityConfig
import taeyun.malanalter.user.UserService


private val logger = KotlinLogging.logger{}
@Component
class JwtAuthenticationFilter(
    val jwtUtil: JwtUtil,
    val userService: UserService
) :OncePerRequestFilter(){

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val openedUrlMatcher = SecurityConfig.getOpenUrlMatchers()
        if(openedUrlMatcher.any { it.matches(request) }){
            logger.info("skip check jwt for url ${request.requestURI}")
            filterChain.doFilter(request,response)
            return
        }
        val authHeader = request.getHeader("Authorization")

        if (authHeader == null || !authHeader.startsWith("Bearer")) {
            logger.info( "No Auth Requested From Host :  ${request.requestURL}")
            throw RuntimeException("No Auth header in request")
        }
        try {
            val jwt = authHeader.substring(7)
            if (jwtUtil.isExpiredToken(jwt)) {
                logger.info{"Expired Token with $jwt"}
                throw RuntimeException("Expired AccessToken")
            }
            val username = jwtUtil.getUsername(jwt)
            if (validUser(username) && SecurityContextHolder.getContext().authentication == null) {
                val authToken = UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    emptyList() // some additional role authorities
                )
                authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                SecurityContextHolder.getContext().authentication = authToken
            } else {
                logger.info("inValid User or Already authenticated user")
            }
        } catch (e: Exception) {
            logger.error{"JWT Authentication failed ${e.message}"}
            throw RuntimeException("Don't Authorized token")
        }
        filterChain.doFilter(request, response)

    }

    private fun validUser(username: String): Boolean{
        return username.isNotBlank() && userService.existByUsername(username)

    }

}
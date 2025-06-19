package taeyun.malanalter.auth

import AuthResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import taeyun.malanalter.auth.dto.RefreshRequest
import taeyun.malanalter.config.exception.AlerterBadRequest
import taeyun.malanalter.config.exception.ErrorCode
import taeyun.malanalter.user.UserService
import java.time.LocalTime
import java.time.ZoneId

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/alerter/auth")
class AuthController(
    val userService: UserService,
    val authService: AuthService,
) {

    @PostMapping("/logout")
    fun logout(request: HttpServletRequest) {
        val tokenFromRequest = JwtUtil.getTokenFromRequest(request)
        authService.logout(tokenFromRequest) // accessToken 을 블랙리스트에 추가
    }

    // 모든 저장된 리프레시 토큰이 만료되는 pr 시점 7일 이후에 refreshRequest 삭제
    @PostMapping("/refresh")
    fun refresh(
        @RequestBody @Valid refreshRequest: RefreshRequest,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): AuthResponse {
        // first check refresh cookie
        val refreshTokenFromCookie = request.cookies?.find { it.name == AlerterCookieUtil.REFRESH_TOKEN_NAME }?.value
        if (refreshTokenFromCookie.isNullOrEmpty()) {
            logger.warn { "이전 버전의 refresh 요청 : ${LocalTime.now(ZoneId.of("Asia/Seoul"))} 유저 ${refreshRequest.userId}" }
        }
        val refreshToken = refreshTokenFromCookie ?: refreshRequest.refreshToken
        ?: throw AlerterBadRequest(ErrorCode.REFRESH_TOKEN_NOT_FOUND, "Refresh token not found in request body")
        val findById = userService.findById(refreshRequest.userId!!)
        return authService.renewToken(findById, refreshToken, response)
    }
}
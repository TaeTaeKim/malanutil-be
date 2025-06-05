package taeyun.malanalter.auth

import AuthResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import taeyun.malanalter.auth.dto.LoginRequest
import taeyun.malanalter.auth.dto.RefreshRequest
import taeyun.malanalter.config.exception.AlerterBadRequest
import taeyun.malanalter.config.exception.ErrorCode
import taeyun.malanalter.user.UserService
import taeyun.malanalter.user.dto.UserRegisterRequest

private val logger = KotlinLogging.logger{}
@RestController
@RequestMapping("/alerter/auth")
class AuthController(
    val userService: UserService,
    val authService: AuthService
) {

    @PostMapping("/register")
    @Deprecated(message = "deprecated", replaceWith = ReplaceWith("/oauth2/authorization/discord"))
    fun register(@RequestBody  @Valid userRegisterRequest: UserRegisterRequest) {
        throw AlerterBadRequest(ErrorCode.DEPRECATED_API,null)
    }

    @PostMapping("/login")
    @Deprecated(message = "deprecated", replaceWith = ReplaceWith("/oauth2/authorization/discord"))
    fun login(@RequestBody @Valid loginRequest: LoginRequest) : ResponseEntity<AuthResponse>{
        throw AlerterBadRequest(ErrorCode.DEPRECATED_API,null)
    }

    @PostMapping("/logout")
    fun logout(request: HttpServletRequest) {
        val tokenFromRequest = JwtUtil.getTokenFromRequest(request)
        authService.logout(tokenFromRequest) // accessToken 을 블랙리스트에 추가
    }

    @PostMapping("/refresh")
    fun refresh(@RequestBody @Valid refreshRequest: RefreshRequest): ResponseEntity<AuthResponse>? {
        // find user
        val foundUser = userService.findById(refreshRequest.getUsername())
        // check
        val renewTokenResponse = authService.renewToken(foundUser, refreshRequest.refreshToken.orEmpty())
        return ResponseEntity.ok(renewTokenResponse)
    }

}
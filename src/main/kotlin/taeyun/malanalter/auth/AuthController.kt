package taeyun.malanalter.auth

import AuthResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import taeyun.malanalter.auth.dto.LoginRequest
import taeyun.malanalter.auth.dto.RefreshRequest
import taeyun.malanalter.config.exception.AlerterServerError
import taeyun.malanalter.user.UserService
import taeyun.malanalter.user.dto.UserRegisterRequest
import java.util.*

private val logger = KotlinLogging.logger{}
@RestController
@RequestMapping("/malan-alter/auth")
class AuthController(
    val userService: UserService,
    val jwtUtil: JwtUtil,
    val authService: AuthService
) {

    @PostMapping("/register")
    fun register(@RequestBody  @Valid userRegisterRequest: UserRegisterRequest) {
        try {
            userService.addUser(userRegisterRequest)
        } catch (e: Exception) {
            val randomUUID = UUID.randomUUID()
            logger.error { "$randomUUID ${e.message}" }
            throw AlerterServerError(message = "[$randomUUID] Unexpected Error in Creating User")
        }
    }

    @PostMapping("/login")
    fun login(@RequestBody @Valid loginRequest: LoginRequest) : ResponseEntity<AuthResponse>{
        val user = userService.loginUser(loginRequest)
        val generateAccessToken = jwtUtil.generateAccessToken(user.username)
        val generateRefreshToken = jwtUtil.generateRefreshToken()
        try {
            authService.registerRefreshToken(user, generateRefreshToken)
            return ResponseEntity.ok(
                AuthResponse(
                    accessToken = generateAccessToken,
                    refreshToken = generateRefreshToken,
                    expireAt = jwtUtil.getExpiryFromToken(generateRefreshToken).time
                )
            )
        }catch (e: Exception) {
            val randomUUID = UUID.randomUUID()
            logger.error { "$randomUUID ${e.message}" }
            throw AlerterServerError(message = "[$randomUUID] Unexpected error in login")
        }

    }

    @PostMapping("/logout")
    fun logout() {
        // Logout logic here
    }

    @PostMapping("/refresh")
    fun refresh(@RequestBody @Valid refreshRequest: RefreshRequest): ResponseEntity<AuthResponse>? {
        // find user
        val foundUser = userService.findByUsername(refreshRequest.username.orEmpty())
        // check
        val renewTokenResponse = authService.renewToken(foundUser, refreshRequest.refreshToken.orEmpty())
        return ResponseEntity.ok(renewTokenResponse)
    }

}
package taeyun.malanalter.auth

import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import taeyun.malanalter.auth.dto.AuthResponse
import taeyun.malanalter.auth.dto.AuthService
import taeyun.malanalter.auth.dto.LoginRequest
import taeyun.malanalter.user.UserService
import taeyun.malanalter.user.dto.UserRegisterRequest

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
            throw IllegalArgumentException("Failed to register user: ${e.message}")
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
                    username = user.username,
                    expireAt = jwtUtil.getExpiryFromToken(generateRefreshToken).time
                )
            )
        }catch (e: Exception) {
            throw IllegalArgumentException("Failed to login user: ${e.message}")
        }

    }

    @PostMapping("/logout")
    fun logout() {
        // Logout logic here
    }

    @PostMapping("/refresh")
    fun refresh() {
        // Token refresh logic here
    }

}
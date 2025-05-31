package taeyun.malanalter.auth

import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import taeyun.malanalter.auth.dto.AuthService
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
        // check user exist
        if (userService.existByUsername(userRegisterRequest.username)) {
            throw IllegalArgumentException("Username already exists")
        }
        try {
            userService.addUser(userRegisterRequest)
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to register user: ${e.message}")
        }
    }

    @PostMapping("/login")
    fun login() {
        // Login logic here
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
package taeyun.malanalter.auth

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.FunSpec
import io.mockk.justRun
import io.mockk.mockk
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import taeyun.malanalter.auth.dto.RefreshRequest
import taeyun.malanalter.config.exception.ErrorCode
import taeyun.malanalter.config.exception.GlobalControllerAdvice
import taeyun.malanalter.feignclient.DiscordAlertClient
import taeyun.malanalter.user.UserService
import taeyun.malanalter.user.domain.UserEntity

class AuthControllerTest : FunSpec({


    val userService = mockk<UserService>()
    val authService = mockk<AuthService>()
    val discordClient = mockk<DiscordAlertClient>()
    val mapper = ObjectMapper()
    val username = 100L
    val mockMvc: MockMvc = MockMvcBuilders.standaloneSetup(AuthController(userService, authService))
        .setControllerAdvice(GlobalControllerAdvice(discordClient))
        .build()
    justRun { discordClient.sendAlarm(any()) }

    val mockUser = mockk<UserEntity>()


    test("http only cookie, request body 모두 없으면 재로그인 에러 응답") {
        val emptyRefreshTokenRequest = RefreshRequest(username, null)

        mockMvc.perform(
            post("/alerter/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(emptyRefreshTokenRequest))
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.code").value(ErrorCode.REFRESH_TOKEN_NOT_FOUND.code))
    }




})

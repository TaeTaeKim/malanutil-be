package taeyun.malanalter.auth

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import jakarta.servlet.FilterChain
import org.jetbrains.exposed.v1.dao.Entity
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.context.SecurityContextImpl
import taeyun.malanalter.user.UserService
import taeyun.malanalter.user.domain.UserEntity

class JwtAuthenticationFilterTest : FunSpec({

    val jwtUtil = mockk<JwtUtil>()
    val userService = mockk<UserService>()
    val jwtFilter = JwtAuthenticationFilter(jwtUtil, userService)
    val filterChain = mockk<FilterChain>(relaxed = true)

    lateinit var request: MockHttpServletRequest
    lateinit var response: MockHttpServletResponse


    beforeTest {
        // 매 테스트마다 SecurityContext 초기화
        SecurityContextHolder.setContext(SecurityContextImpl(null))
        request = MockHttpServletRequest()
        response = MockHttpServletResponse()
    }

    afterTest {
        SecurityContextHolder.clearContext()
    }

    test("공개된 URL 은 필터를 건너뛴다 -> SecurityContext 가 null 이다"){

        request.servletPath = "/swagger-ui/test-uri"
        request.method = "GET"
        shouldNotThrow<RuntimeException>{ jwtFilter.doFilter(request, response, filterChain) }
        SecurityContextHolder.getContext().authentication.shouldBeNull()
    }


    test("인증된 URI에 헤더가 없으면 Exception 반환"){
        request.servletPath = "/authenticated/uri"

        shouldThrow<RuntimeException> { jwtFilter.doFilter(request, response, filterChain) }
        SecurityContextHolder.getContext().authentication.shouldBeNull()
    }

    test("만료된 토큰인 경우에는 Exception 이 반환된다."){
        val fakeToken = "expired.jwt.token"
        every { jwtUtil.isExpiredToken(fakeToken) } returns true


        request.servletPath = "/authenticated/uri"
        request.addHeader("Authorization", "Bearer $fakeToken")

        shouldThrow<RuntimeException> { jwtFilter.doFilter(request, response, filterChain) }
        SecurityContextHolder.getContext().authentication.shouldBeNull()
    }

    test("유효한 토큰 + 정상적인 사용자일 경우 Context에 Authencation 이 생기고 다음 필터체인 호출한다.") {
        val fakeToken = "valid.jwt.token"
        val username = "alice"
        val userEntity = mockk<UserEntity> ()

        every { jwtUtil.isExpiredToken(fakeToken) } returns false
        every { jwtUtil.getUsername(fakeToken) } returns username
        every { userService.existByUsername(username) } returns true
        every { userService.isLogoutUser((fakeToken)) } returns false
        every { userService.findByUsername(username) } returns userEntity
        every { userEntity.userId.value } returns 1
        every { userEntity.username } returns username

        request.servletPath = "/authenticated/uri"
        request.addHeader("Authorization", "Bearer $fakeToken")

        jwtFilter.doFilter(request, response, filterChain)

        val authentication = SecurityContextHolder.getContext().authentication
        authentication.shouldNotBeNull()
        authentication.shouldBeInstanceOf<UsernamePasswordAuthenticationToken>()
        authentication.name shouldBe username
    }




})

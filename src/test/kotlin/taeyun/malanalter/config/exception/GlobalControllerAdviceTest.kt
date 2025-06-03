package taeyun.malanalter.config.exception

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.bind.annotation.GetMapping

class GlobalControllerAdviceTest : FunSpec({

    lateinit var mockMvc: MockMvc

    mockMvc = MockMvcBuilders.standaloneSetup(TestController())
        .setControllerAdvice(GlobalControllerAdvice())
        .build()

    test("BaseException 핸들링 - Cause의 메세지가 있으면 Cause의 메세지 출력"){
        val result = mockMvc.perform(get("/test/base-exception/message").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized)
            .andReturn()
        val contentAsString = result.response.contentAsString
        contentAsString.contains("JwtException") shouldBe true
        contentAsString.contains(ErrorCode.INVALID_TOKEN.code) shouldBe true
    }

    test("BaseException 핸들링 - Cause 메세지가 없으면 default 메세지 출력"){
        val result = mockMvc.perform(get("/test/base-exception/default-message").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized)
            .andReturn()
        val contentAsString = result.response.contentAsString
        contentAsString.contains(ErrorCode.INVALID_TOKEN.defaultMessage) shouldBe true
        contentAsString.contains(ErrorCode.INVALID_TOKEN.code) shouldBe true
    }

    test("BaseException 이 아닌 RunTimeException 발생시에도 handling"){
        val result = mockMvc.perform(get("/test/internal-server-error").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError)
            .andReturn()

        result.response.contentAsString.contains(ErrorCode.INTERNAL_SERVER_ERROR.defaultMessage) shouldBe true
    }


})
@Controller
class TestController{
    @GetMapping("/test/base-exception/message")
    fun throwBaseExceptionWithMessage() {
        throw AlerterJwtException(ErrorCode.INVALID_TOKEN, message = "JwtException")
    }

    @GetMapping("/test/base-exception/default-message")
    fun throwBaseExceptionDefaultMessage() {
        throw AlerterJwtException(ErrorCode.INVALID_TOKEN, message = null)
    }

    @GetMapping("/test/internal-server-error")
    fun throwInternalServerError() {
        throw RuntimeException()
    }
}

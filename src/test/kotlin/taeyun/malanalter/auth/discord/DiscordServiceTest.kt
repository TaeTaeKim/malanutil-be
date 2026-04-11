package taeyun.malanalter.auth.discord

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class DiscordServiceTest : StringSpec({

    "URL 추출 정규식 테스트"{
        val urlPattern = DiscordService.URL_PATTERN
        val message = "여기 링크가 있습니다: [링크](https://mapleland.gg/trade/test-url)"
        val matchResult = urlPattern.find(message)
        matchResult!!.groupValues[1] shouldBe "test-url"
    }

    // isLeftUser 함수는 Discord 에러 문자열을 보고 "사용자가 서버를 탈퇴했거나 DM 수신 불가 상태인지" 판별한다
    "isLeftUser - 50278 코드가 포함된 에러는 true" {
        // Discord 에러 코드 50278이 에러 메시지에 포함된 케이스
        DiscordService.isLeftUser("ErrorResponseException: 50278: Something failed") shouldBe true
    }

    "isLeftUser - CANNOT_SEND_TO_USER 문자열이 포함된 에러는 true" {
        // Discord가 반환하는 CANNOT_SEND_TO_USER 에러 상수 케이스
        DiscordService.isLeftUser("net.dv8tion.jda.api.exceptions.ErrorResponseException: CANNOT_SEND_TO_USER") shouldBe true
    }

    "isLeftUser - 관련 없는 에러는 false" {
        // 전혀 다른 에러 메시지는 사용자 탈퇴와 무관하므로 false
        DiscordService.isLeftUser("java.net.SocketTimeoutException: Read timed out") shouldBe false
    }

    "isLeftUser - 빈 문자열은 false" {
        // 경계값: 빈 문자열은 매칭되지 않아야 함
        DiscordService.isLeftUser("") shouldBe false
    }

    "isLeftUser - 50007 코드(Cannot send messages to this user)가 포함된 에러는 true" {
        // Discord 공식 에러 코드 50007: DM이 비활성화되었거나 봇을 차단한 사용자
        DiscordService.isLeftUser("ErrorResponseException: 50007: Cannot send messages to this user") shouldBe true
    }

})

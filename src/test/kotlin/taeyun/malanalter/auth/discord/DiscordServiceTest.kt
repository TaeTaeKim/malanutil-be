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


})

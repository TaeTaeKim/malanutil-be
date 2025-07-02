package taeyun.malanalter.feignclient

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.ints.gte
import io.kotest.matchers.ints.lte
import io.kotest.matchers.shouldBe
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.test.context.TestConstructor
import org.springframework.test.context.TestPropertySource
import taeyun.malanalter.alertitem.dto.ItemCondition
import taeyun.malanalter.alertitem.dto.MalanggBidRequest

@FeignTest
@TestPropertySource(properties = ["alerter.discord.webhook-url=test"])
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@EnableFeignClients
class FeignClientTest(private val malanClient: MalanClient) : StringSpec({
    "Malan Client로 null 필드가k 잘 요청되는지 확인" {
        val itemCode = 1432013
        val itemCondition = ItemCondition(
            lowPrice = 10000,
            highPrice = 2000000,
            str = null,
            dex = null,
            int = null,
            luk = null,
            pad = null,
            mad = null,
            hapma = null,
            speed = null,
            accuracy = 17
        )
        val malanggBidRequest = MalanggBidRequest(itemCondition)
        // not make exception in malanClient.getItemBidList
        shouldNotThrow<Exception>{
            val itemBidList = malanClient.getItemBidList(itemCode, malanggBidRequest)
            println(itemBidList)
        }
    }

    "price 범위를 설정하면 그 사이 매물만 검색 "{
        val itemCode = 1382007 // 이블윙즈
        val itemCondition = ItemCondition(
            lowPrice = 4000000,
            highPrice = 5000000
        )
        val malanggBidRequest = MalanggBidRequest(itemCondition)
        shouldNotThrow<Exception> {
            val itemBidList = malanClient.getItemBidList(itemCode, malanggBidRequest)
            itemBidList.forEach { item ->
                item.itemPrice shouldBe gte(itemCondition.lowPrice!!)
                item.itemPrice shouldBe lte(itemCondition.highPrice!!)
            }

        }

    }
})
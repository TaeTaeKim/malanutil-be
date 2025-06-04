package taeyun.malanalter.feignclient

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.core.spec.style.StringSpec
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.test.context.TestConstructor
import taeyun.malanalter.alertitem.dto.ItemCondition
import taeyun.malanalter.alertitem.dto.MalanggBidRequest

@FeignTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@EnableFeignClients
class FeignClientTest(private val malanClient: MalanClient) : StringSpec({
    "Malan Client로 null 필드가k 잘 요청되는지 확인" {
        val itemCode = 2044002
        val itemCondition = ItemCondition(
            price = 100,
            str = null,
            dex = null,
            int = null,
            luk = null,
            pad = null,
            mad = null,
            hapma = null,
            speed = null,
            accuracy = null
        )
        val malanggBidRequest = MalanggBidRequest(itemCondition)
        // not make exception in malanClient.getItemBidList
        shouldNotThrow<Exception>{
            malanClient.getItemBidList(itemCode, malanggBidRequest)
        }
    }
})
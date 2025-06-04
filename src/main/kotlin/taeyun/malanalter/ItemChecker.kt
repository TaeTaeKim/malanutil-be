package taeyun.malanalter

import lombok.RequiredArgsConstructor
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import taeyun.malanalter.alertitem.dto.DiscordMessage
import taeyun.malanalter.alertitem.dto.ItemBidInfo
import taeyun.malanalter.alertitem.dto.MalanggBidRequest
import taeyun.malanalter.feignclient.DiscordClient
import taeyun.malanalter.feignclient.MalanClient
import taeyun.malanalter.alertitem.repository.AlertRepository
import java.time.LocalDateTime
import java.time.ZoneId

@Component
@RequiredArgsConstructor
class ItemChecker(
    private val alertRepository: AlertRepository,
    private val malanClient: MalanClient,
    private val discordClient: DiscordClient
) {

    @Scheduled(fixedRate = 1000 * 60 * 10, initialDelay = 5000L) // 10분마다 실행, 초기 지연 5초
    fun checkItem() {
        // 한국 새벽 시간에는 1시간 간격으로 체크 (서버 시간은 UTC)
        LocalDateTime.now(ZoneId.of("Asia/Seoul")).let {
            if (it.hour in 0..10) return
        }
        val checkItemIdAndPriceMap = alertRepository.getRegisteredItem()
        checkItemIdAndPriceMap.forEach { (itemId, itemCondition) ->
            val sellingBids = malanClient.getItemBidList(itemId, MalanggBidRequest(itemCondition))
                .filter { it.tradeType == ItemBidInfo.TradeType.SELL && it.tradeStatus }
                .sortedBy { it.itemPrice.inc() }
            if (sellingBids.isNotEmpty()) {
                discordClient.sendDiscordMessage(DiscordMessage(sellingBids))
            }
        }
    }

}
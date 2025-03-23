package taeyun.malanalter

import lombok.RequiredArgsConstructor
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneId

@Component
@RequiredArgsConstructor
class ItemChecker(
    private val checkRepository: CheckRepository,
    private val malanClient: MalanClient,
    private val discordClient: DiscordClient
) {

    @Scheduled(fixedRate = 1000 * 60 * 30) // 30분마다 실행 (초기 1초 후)
    fun checkItem() {
        // 한국 새벽 시간에는 1시간 간격으로 체크 (서버 시간은 UTC)
        LocalDateTime.now(ZoneId.of("Asia/Seoul")).let {
            if (it.hour in 0..10) return
        }
        val checkItemIdAndPriceMap = checkRepository.getCheckItemIdAndPriceMap()
        checkItemIdAndPriceMap.forEach { (itemId, itemCondition) ->
            val sellingBids = malanClient.getItemBidList(itemId, itemCondition)
                .filter { it.tradeType == ItemBidInfo.TradeType.SELL && it.tradeStatus }
                .sortedBy { it.itemPrice.inc() }
            if (sellingBids.isNotEmpty()) {
                val create = DiscordMessage.create(sellingBids)
                checkRepository.saveItemName(itemId, sellingBids[0].itemName)
                discordClient.sendDiscordMessage(create)
            }
        }
    }

}
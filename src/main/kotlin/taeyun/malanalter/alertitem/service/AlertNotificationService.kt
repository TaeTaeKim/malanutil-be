package taeyun.malanalter.alertitem.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import taeyun.malanalter.alertitem.dto.DiscordMessageContainer
import taeyun.malanalter.alertitem.dto.ItemBidInfo
import taeyun.malanalter.auth.discord.DiscordService

private val logger = KotlinLogging.logger { }

/**
 * 비드 결과를 Discord 메시지로 변환하여 유저에게 DM을 발송한다.
 */
@Service
class AlertNotificationService(
    private val discordService: DiscordService
) {
    // 비드 결과를 청크 분할하여 Discord DM 발송
    fun sendBidAlerts(userId: Long, bidResults: List<Pair<Int, List<ItemBidInfo>>>) {
        val messageContainer = DiscordMessageContainer()
        bidResults.forEach { (itemId, bids) ->
            messageContainer.addBids(itemId, bids)
        }

        val chunkedMessageList: List<String> = messageContainer.getMessageContentList()
        if (chunkedMessageList.isNotEmpty()) {
            chunkedMessageList.forEach { discordService.sendDirectMessage(userId, it) }
            logger.debug { "[Notification] Sent ${chunkedMessageList.size} messages to User:$userId" }
        }
    }
}
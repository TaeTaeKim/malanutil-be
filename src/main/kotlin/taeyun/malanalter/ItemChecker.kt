package taeyun.malanalter

import io.github.oshai.kotlinlogging.KotlinLogging
import lombok.RequiredArgsConstructor
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import taeyun.malanalter.alertitem.dto.*
import taeyun.malanalter.alertitem.repository.AlertRepository
import taeyun.malanalter.auth.discord.DiscordService
import taeyun.malanalter.feignclient.MalanClient
import taeyun.malanalter.user.UserService
import taeyun.malanalter.user.domain.UserEntity

private val logger = KotlinLogging.logger { }

@Component
@RequiredArgsConstructor
class ItemChecker(
    private val alertRepository: AlertRepository,
    private val malanClient: MalanClient,
    private val userService: UserService,
    private val discordService: DiscordService
) {
    @Scheduled(fixedRate = 1000 * 60 * 5, initialDelay = 5000L)
    fun checkItemV2() {
        val allUserEntityMap: Map<Long, UserEntity> = userService.getAllUserEntityMap()
        val itemsByUser = alertRepository.getRegisteredItem().groupBy { it.userId }

        // user 별로 discord 보내는 로직 -> 특정 사람이 보내지지 않아도 그래도 보내야한다.
        itemsByUser.forEach { (userId, items) ->
            // 존재하는 유저 확인
            val userEntity = allUserEntityMap[userId] ?: run {
                logger.warn { "User:$userId not found" }
                return@forEach
            }
            // ─── “지금이 이 사용자의 알람 시간인지”  체크 ───
            if (userEntity.isNotAlarmTime()) return@forEach
            val discordMessage = DiscordMessage()
            // 실제 알람 로직: isAlarm 플래그가 true인 것만 필터해서 처리
            items.filter { it.isAlarm }
                .forEach { discordMessage.addBids(it.id, itemBidInfos(it)) }
            val messageList = discordMessage.getDiscordMessageContents()
            messageList.forEach { discordService.sendDirectMessage(userId, it) }
        }
    }

    private fun itemBidInfos(item: RegisteredItem): List<ItemBidInfo> {
        try {
            return malanClient.getItemBidList(item.itemId, MalanggBidRequest(item.itemOptions))
                .filter { bids -> bids.tradeType == ItemBidInfo.TradeType.SELL && bids.tradeStatus }
                .sortedBy { it.itemPrice.inc() }
        } catch (e: Exception) {
            logger.error { "Error in Request to Malangg ${e.message}" }
            return emptyList()
        }
    }
}
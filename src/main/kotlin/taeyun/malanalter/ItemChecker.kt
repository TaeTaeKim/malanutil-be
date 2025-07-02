package taeyun.malanalter

import io.github.oshai.kotlinlogging.KotlinLogging
import lombok.RequiredArgsConstructor
import org.jetbrains.annotations.VisibleForTesting
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import taeyun.malanalter.alertitem.domain.ItemBidEntity
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
        val savedBidsByItemId: Map<Int, List<ItemBidEntity>> = alertRepository.getAllItemComments().groupBy { it.alertItemId }

        // user 별로 discord 보내는 로직 -> 특정 사람이 보내지지 않아도 그래도 보내야한다.
        itemsByUser.forEach { (userId, registeredItems) ->
            // 존재하는 유저 확인
            val userEntity = allUserEntityMap[userId] ?: run {
                logger.warn { "User:$userId not found" }
                return@forEach
            }
            // ─── “지금이 이 사용자의 알람 시간인지”  체크 ───
            if (userEntity.isNotAlarmTime()) return@forEach
            val messageContainer = DiscordMessageContainer()
            // 알람 true인 아이템들만 순회
            // 순회하면서 응답된 bid 5개까지 container에 저장
            registeredItems.filter { it.isAlarm }
                .forEach { messageContainer.addBids(it.id, requestItemBids(it, savedBidsByItemId[it.id] ?: emptyList())) }
            val chunkedMessageList:List<String> = messageContainer.getMessageContentList()
            if (chunkedMessageList.isNotEmpty()) {
                chunkedMessageList.forEach { discordService.sendDirectMessage(userId, it) }
            }
        }
    }

    /**
     * 실제로 메랜지지에 아이템 비드를 요청하는 로직
     * 기존에 가지고 있던 비드 리스트를 업데이트하고 알람끈 내역은 반환하지 않는다.
     */
    @VisibleForTesting
    internal fun requestItemBids(item: RegisteredItem, existBidList: List<ItemBidEntity>): List<ItemBidInfo> {
        try {
            val detectedBids: List<ItemBidInfo> =
                malanClient.getItemBidList(item.itemId, MalanggBidRequest(item.itemOptions))
                    .filter { bids -> bids.tradeType == ItemBidInfo.TradeType.SELL && bids.tradeStatus }
                    .sortedBy { it.itemPrice.inc() }
                    .take(100)
            // 기존 Bid info 새로운 bidInfo Sync
            alertRepository.syncBids(item.id, detectedBids, existBidList)
            // 모든 알람에서 울려야하는 알람 5개만 반환
            return detectedBids
                .filter { isAlarmComment(existBidList, it.url) }
                .take(5)
        } catch (e: Exception) {
            logger.error { "Error in Request to Malangg ${e.message}" }
            return emptyList()
        }
    }

    private fun isAlarmComment(existComment: List<ItemBidEntity>, url: String): Boolean {
        // 새로운 bid 이거나 Alarm이 on 된 bid만 리턴
        return  !existComment.map { it.url }.contains(url) || existComment.filter { it.isAlarm }.map { it.url }.contains(url)
    }
}
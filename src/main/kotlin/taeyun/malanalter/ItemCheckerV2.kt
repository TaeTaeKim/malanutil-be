package taeyun.malanalter

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
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
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger { }

@Component
@RequiredArgsConstructor
class ItemCheckerV2(
    private val alertRepository: AlertRepository,
    private val malanClient: MalanClient,
    private val userService: UserService,
    private val discordService: DiscordService,
) {
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    @Scheduled(fixedRate = 1000 * 60 * 5, initialDelay = 1000 * 60 * 5)
    fun callCheckItem() {
        val time = measureTimeMillis {
            val checkItem = checkItem()
            runBlocking {
                checkItem.join() // Wait for the job to complete
            }
        }
        if (time > 1000 * 3) {
            logger.error { "[Scheduler] checkItem took too long: $time ms" }
        }

    }


    fun checkItem(): Job {
        logger.debug { "[Scheduler] Starting item check on thread: ${Thread.currentThread().name}" }
        return coroutineScope.launch {
            logger.debug { "[Coroutine] Executing main task on thread: ${Thread.currentThread().name}" }
            val allUserEntityMap: Map<Long, UserEntity> = userService.getAllUserEntityMap()
            val itemsByUser = alertRepository.getRegisteredItem().groupBy { it.userId }
            val savedBidsByItemId: Map<Int, List<ItemBidEntity>> =
                alertRepository.getAllItemComments().groupBy { it.alertItemId }

            itemsByUser.forEach { (userId, registeredItems) ->
                launch {
                    logger.debug { "[User Coroutine] Start processing for User:$userId on thread: ${Thread.currentThread().name}" }
                    val userEntity = allUserEntityMap[userId] ?: return@launch
                    if (userEntity.isAlarmOff() || userEntity.isNotAlarmTime()) return@launch

                    val messageContainer = DiscordMessageContainer()
                    val deferredBids = registeredItems
                        .filter { it.isAlarm }
                        .map { item ->
                            async {
                                val bids = requestItemBids(item, savedBidsByItemId[item.id] ?: emptyList())
                                Pair(item.id, bids)
                            }
                        }

                    val bidResults = deferredBids.awaitAll()
                    logger.debug { "[User Coroutine] Fetched all bids for User:$userId on thread: ${Thread.currentThread().name}" }

                    bidResults.forEach { (itemId, bids) ->
                        messageContainer.addBids(itemId, bids)
                    }

                    val chunkedMessageList: List<String> = messageContainer.getMessageContentList()
                    if (chunkedMessageList.isNotEmpty()) {
                        chunkedMessageList.forEach { discordService.sendDirectMessage(userId, it) }
                        logger.debug { "[User Coroutine] Sent ${chunkedMessageList.size} messages to User:$userId" }
                    }
                }
            }
        }
    }

    /**
     * @return 가격순으로 알람이 켜져있고 보내지 않은 비드를 최대 5개까지 반환한다.
     * 실제로 메랜지지에 아이템 비드를 요청하는 로직
     * 기존에 가지고 있던 비드 리스트를 업데이트하고 알람끈 내역은 반환하지 않는다.
     */
    @VisibleForTesting
    internal suspend fun requestItemBids(item: RegisteredItem, existBidList: List<ItemBidEntity>): List<ItemBidInfo> =
        withContext(Dispatchers.IO) {
            logger.debug { "[Item Request] Fetching bids for Item:${item.id} on thread: ${Thread.currentThread().name}" }
            try {
                val detectedBids: List<ItemBidInfo> =
                    malanClient.getItemBidList(item.itemId, MalanggBidRequest(item.itemOptions))
                        .filter { bids -> bids.tradeType == item.tradeType && bids.tradeStatus }
                        .sortedWith(
                            if (item.tradeType == TradeType.BUY) {
                                compareByDescending<ItemBidInfo> { it.itemPrice }.thenBy { it.comment }
                            } else {
                                compareBy<ItemBidInfo> { it.itemPrice }.thenBy { it.comment }
                            }
                        )
                        .take(100)
                // 기존 Bid info 새로운 bidInfo Sync
                alertRepository.syncBids(item.id, detectedBids, existBidList)
                // 모든 비드에서 보내야할 알람 반환
                return@withContext detectedBids
                    .filter { isAlarmComment(existBidList, it.url) }
                    .take(5)
                    .filter { notSentAlarm(existBidList, it.url) }
            } catch (e: Exception) {
                logger.error { "Error in Request to Malangg ${e.message}" }
                return@withContext emptyList()
            }
        }

    private fun notSentAlarm(
        existBidList: List<ItemBidEntity>,
        url: String
    ): Boolean {
        val existingBid = existBidList.find { it.url == url }
        return existingBid == null || !existingBid.isSent // 새로운 비드이거나 한번도 알람을 보내지 않은 경우
    }

    private fun isAlarmComment(existComment: List<ItemBidEntity>, bidId: String): Boolean {
        val existingBid = existComment.find { it.url == bidId }
        return existingBid == null || (existingBid.isAlarm) // 알람이 켜져있거나 기존에 없던 비드인 경우
    }
}
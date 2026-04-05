package taeyun.malanalter

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import lombok.RequiredArgsConstructor
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import taeyun.malanalter.alertitem.domain.ItemBidEntity
import taeyun.malanalter.alertitem.dto.DiscordMessageContainer
import taeyun.malanalter.alertitem.repository.AlertRepository
import taeyun.malanalter.alertitem.service.BidDetectService
import taeyun.malanalter.auth.discord.DiscordService
import taeyun.malanalter.config.MetricsService
import taeyun.malanalter.config.exception.ErrorNotification
import taeyun.malanalter.feignclient.DiscordAlertClient
import taeyun.malanalter.user.UserService
import taeyun.malanalter.user.domain.UserEntity
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger { }

@Component
@RequiredArgsConstructor
class ItemCheckerV2(
    private val alertRepository: AlertRepository,
    private val alertClient: DiscordAlertClient,
    private val userService: UserService,
    private val discordService: DiscordService,
    private val metricsService: MetricsService,
    private val bidDetectService: BidDetectService
) : ItemChecker {
    // SupervisorJob: 자식 코루틴의 예외가 부모 스코프를 취소하지 않도록 방지
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

//    @Scheduled(fixedRate = 1000 * 60 * 5, initialDelay = 1000 * 60 * 5)
    @Scheduled(fixedRate = 1000 * 60 * 5)
    fun callCheckItem() {
        metricsService.resetCycleMetrics()
        val time = measureTimeMillis {
            val checkItem = checkItem()
            runBlocking {
                checkItem.join() // Wait for the job to complete
            }
        }
        metricsService.recordAlertProcessingTime(time)
        if (time > 1000 * 3) {
            logger.error { "[Scheduler] checkItem took too long: $time ms" }
        }

    }


    override fun checkItem(): Job {
        logger.debug { "[Scheduler] Starting item check on thread: ${Thread.currentThread().name}" }
        return coroutineScope.launch {
            try {
                logger.debug { "[Coroutine] Executing main task on thread: ${Thread.currentThread().name}" }
                val allUserEntityMap: Map<Long, UserEntity> = userService.getAllUserEntityMap()
                val itemsByUser = alertRepository.getRegisteredItem().groupBy { it.userId }
                val savedBidsByItemId: Map<Int, List<ItemBidEntity>> =
                    alertRepository.getAllItemComments().groupBy { it.alertItemId.value }

                // 등록된 아이템이 없으면 로그 남기고 종료
                if (itemsByUser.isEmpty()) {
                    logger.warn { "[Scheduler] No registered items found. Skipping check." }
                    return@launch
                }
                logger.info { "[Scheduler] Processing ${itemsByUser.size} users with registered items" }

                itemsByUser.forEach { (userId, registeredItems) ->
                    launch {
                        logger.debug { "[User Coroutine] Start processing for User:$userId on thread: ${Thread.currentThread().name}" }
                        val userEntity = allUserEntityMap[userId] ?: return@launch
                        if (userEntity.disabled) {
                            logger.debug { "[User Coroutine] User:$userId is disabled, skipping." }
                            return@launch
                        }
                        if (userEntity.isAlarmOff() || userEntity.isNotAlarmTime()) {
                            logger.info { "[User Coroutine] User:$userId skipped - alarmOff:${userEntity.isAlarmOff()} notAlarmTime:${userEntity.isNotAlarmTime()}" }
                            return@launch
                        }

                        val alarmItems = registeredItems.filter { it.isAlarm }
                        if (alarmItems.isEmpty()) {
                            logger.info { "[User Coroutine] User:$userId has no alarm-enabled items, skipping." }
                            return@launch
                        }

                        val messageContainer = DiscordMessageContainer()
                        val deferredBids = alarmItems
                            .map { item ->
                                async {
                                    val bids = bidDetectService.fetchAlarmsForItem(item, savedBidsByItemId[item.id] ?: emptyList())
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
            } catch (e: Exception) {
                // DB 연결 실패 등 coroutine 내부 예외를 로깅
                logger.error(e) { "[Scheduler] checkItem coroutine failed: ${e.message}" }
                alertClient.sendAlarm(ErrorNotification.fromException(e))
            }
        }
    }


}
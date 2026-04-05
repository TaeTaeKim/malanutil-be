package taeyun.malanalter

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import org.springframework.stereotype.Component
import taeyun.malanalter.alertitem.domain.ItemBidEntity
import taeyun.malanalter.alertitem.dto.ItemBidInfo
import taeyun.malanalter.alertitem.dto.RegisteredItem
import taeyun.malanalter.alertitem.repository.AlertRepository
import taeyun.malanalter.alertitem.service.AlertNotificationService
import taeyun.malanalter.alertitem.service.BidDetectService
import taeyun.malanalter.config.exception.ErrorNotification
import taeyun.malanalter.feignclient.DiscordAlertClient
import taeyun.malanalter.user.UserService
import taeyun.malanalter.user.domain.UserEntity

private val logger = KotlinLogging.logger { }

@Component
class ItemCheckerV2(
    private val alertRepository: AlertRepository,
    private val alertClient: DiscordAlertClient,
    private val userService: UserService,
    private val bidDetectService: BidDetectService,
    private val alertNotificationService: AlertNotificationService
) : ItemChecker {
    // SupervisorJob: 자식 코루틴의 예외가 부모 스코프를 취소하지 않도록 방지
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun checkItem(): Job {
        logger.debug { "[Scheduler] Starting item check on thread: ${Thread.currentThread().name}" }
        return coroutineScope.launch {
            try {
                logger.debug { "[Coroutine] Executing main task on thread: ${Thread.currentThread().name}" }
                val allUserEntityMap: Map<Long, UserEntity> = userService.getAllUserEntityMap()
                val itemsByUser = alertRepository.getRegisteredItem().groupBy { it.userId }
                val savedBidsByItemId: Map<Int, List<ItemBidEntity>> =
                    alertRepository.getAllItemComments().groupBy { it.alertItemId.value }

                if (itemsByUser.isEmpty()) {
                    logger.warn { "[Scheduler] No registered items found. Skipping check." }
                    return@launch
                }
                logger.info { "[Scheduler] Processing ${itemsByUser.size} users with registered items" }

                itemsByUser.forEach { (userId, registeredItems) ->
                    launch { processUserItems(userId, allUserEntityMap[userId], registeredItems, savedBidsByItemId) }
                }
            } catch (e: Exception) {
                logger.error(e) { "[Scheduler] checkItem coroutine failed: ${e.message}" }
                alertClient.sendAlarm(ErrorNotification.fromException(e))
            }
        }
    }

    // 유저별 아이템 비드 조회 및 알림 발송
    private suspend fun CoroutineScope.processUserItems(
        userId: Long,
        userEntity: UserEntity?,
        registeredItems: List<RegisteredItem>,
        savedBidsByItemId: Map<Int, List<ItemBidEntity>>
    ) {
        logger.debug { "[User Coroutine] Start processing for User:$userId on thread: ${Thread.currentThread().name}" }
        if (!shouldProcessUser(userId, userEntity)) return

        val alarmItems = registeredItems.filter { it.isAlarm }
        if (alarmItems.isEmpty()) {
            logger.info { "[User Coroutine] User:$userId has no alarm-enabled items, skipping." }
            return
        }

        val bidResults = fetchBidsAsync(alarmItems, savedBidsByItemId)
        logger.debug { "[User Coroutine] Fetched all bids for User:$userId on thread: ${Thread.currentThread().name}" }

        alertNotificationService.sendBidAlerts(userId, bidResults)
    }

    // 유저가 알림을 받을 수 있는 상태인지 확인
    private fun shouldProcessUser(userId: Long, userEntity: UserEntity?): Boolean {
        if (userEntity == null) return false
        if (userEntity.disabled) {
            logger.debug { "[User Coroutine] User:$userId is disabled, skipping." }
            return false
        }
        if (userEntity.isAlarmOff() || userEntity.isNotAlarmTime()) {
            logger.info { "[User Coroutine] User:$userId skipped - alarmOff:${userEntity.isAlarmOff()} notAlarmTime:${userEntity.isNotAlarmTime()}" }
            return false
        }
        return true
    }

    // 각 아이템의 비드를 비동기로 조회
    private suspend fun CoroutineScope.fetchBidsAsync(
        alarmItems: List<RegisteredItem>,
        savedBidsByItemId: Map<Int, List<ItemBidEntity>>
    ): List<Pair<Int, List<ItemBidInfo>>> =
        alarmItems
            .map { item ->
                async {
                    val bids = bidDetectService.fetchAlarmsForItem(item, savedBidsByItemId[item.id] ?: emptyList())
                    Pair(item.id, bids)
                }
            }
            .awaitAll()
}
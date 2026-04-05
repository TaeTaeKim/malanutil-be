package taeyun.malanalter

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import lombok.RequiredArgsConstructor
import org.springframework.stereotype.Component
import taeyun.malanalter.alertitem.domain.ItemBidEntity
import taeyun.malanalter.alertitem.repository.AlertRepository
import taeyun.malanalter.alertitem.service.AlertNotificationService
import taeyun.malanalter.alertitem.service.BidDetectService
import taeyun.malanalter.config.exception.ErrorNotification
import taeyun.malanalter.feignclient.DiscordAlertClient
import taeyun.malanalter.user.UserService
import taeyun.malanalter.user.domain.UserEntity

private val logger = KotlinLogging.logger { }

@Component
@RequiredArgsConstructor
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

                        // 각 아이템의 비드를 비동기로 조회
                        val bidResults = alarmItems
                            .map { item ->
                                async {
                                    val bids = bidDetectService.fetchAlarmsForItem(item, savedBidsByItemId[item.id] ?: emptyList())
                                    Pair(item.id, bids)
                                }
                            }
                            .awaitAll()
                        logger.debug { "[User Coroutine] Fetched all bids for User:$userId on thread: ${Thread.currentThread().name}" }

                        // 비드 결과를 Discord DM으로 발송
                        alertNotificationService.sendBidAlerts(userId, bidResults)
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
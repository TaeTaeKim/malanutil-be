package taeyun.malanalter.alertitem.service

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import taeyun.malanalter.alertitem.domain.ItemBidEntity
import taeyun.malanalter.alertitem.dto.ItemBidInfo
import taeyun.malanalter.alertitem.dto.MalanggBidRequest
import taeyun.malanalter.alertitem.dto.RegisteredItem
import taeyun.malanalter.alertitem.dto.TradeType
import taeyun.malanalter.alertitem.repository.AlertRepository
import taeyun.malanalter.config.MetricsService
import taeyun.malanalter.config.exception.ErrorNotification
import taeyun.malanalter.feignclient.DiscordAlertClient
import taeyun.malanalter.feignclient.MalanClient

/**
 * @return 가격순으로 알람이 켜져있고 보내지 않은 비드를 최대 5개까지 반환한다.
 * 메랜지지 API를 통한 아이템의 비드를 호출
 * DB와 비드 동기화 후 알람대상 비드를 반환한다.
 */
private val logger = KotlinLogging.logger { }

@Service
class BidDetectService(
    private val metricsService: MetricsService,
    private val malanClient: MalanClient,
    private val alertRepository: AlertRepository,
    private val alertClient: DiscordAlertClient,
) {


    suspend fun fetchAlarmsForItem(item: RegisteredItem, existBidList: List<ItemBidEntity>): List<ItemBidInfo> =
        withContext(Dispatchers.IO) {
            logger.debug { "[Item Request] Fetching bids for Item:${item.id} on thread: ${Thread.currentThread().name}" }
            try {
                metricsService.incrementMalanggApiCall()
                val startTime = System.currentTimeMillis()

                val detectedBids: List<ItemBidInfo> =
                    malanClient.getItemBidList(item.itemId, MalanggBidRequest(item.itemOptions))
                        .orEmpty()
                        .filter { bids -> bids.tradeType == item.tradeType && bids.tradeStatus }
                        .sortedWith(
                            if (item.tradeType == TradeType.BUY) {
                                compareByDescending<ItemBidInfo> { it.itemPrice }
                            } else {
                                compareBy<ItemBidInfo> { it.itemPrice }
                            }
                        )
                        .take(100)

                metricsService.recordMalanggApiTime(System.currentTimeMillis() - startTime)

                // 기존 Bid info 새로운 bidInfo Sync
                alertRepository.syncBids(item.id, detectedBids, existBidList)
                // 모든 비드에서 보내야할 알람 반환
                return@withContext detectedBids
                    .filter { BidAlarmFilter.isAlarmEnabled(existBidList, it.url) }
                    .take(5)
                    .filter { BidAlarmFilter.isNotYetSent(existBidList, it.url) }
            } catch (e: Exception) {
                metricsService.incrementMalanggApiFailure()
                alertClient.sendAlarm(ErrorNotification.fromException(e))
                logger.error { "Error in Request to Malangg : ItemId : ${item.id} [${item.itemId}] ErrorMsg : ${e.message} ${e.stackTraceToString()}" }
                return@withContext emptyList()
            }

        }

}
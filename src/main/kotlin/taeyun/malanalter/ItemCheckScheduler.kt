package taeyun.malanalter

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import taeyun.malanalter.config.MetricsService
import java.time.LocalDateTime
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger { }

/**
 * 주기적으로 아이템 체크를 실행하고 소요 시간을 메트릭에 기록하는 스케줄러
 */
@Component
class ItemCheckScheduler(
    private val itemChecker: ItemChecker,
    private val metricsService: MetricsService
) {

    @Scheduled(fixedRate = 1000 * 60)
    fun callCheckItem() {
        metricsService.resetCycleMetrics()
        val scheduleIdx = LocalDateTime.now().minute % 5;
        val time = measureTimeMillis {
            val checkItem = itemChecker.checkItem(scheduleIdx)
            runBlocking {
                checkItem.join()
            }
        }
        metricsService.recordAlertProcessingTime(time)
        if (time > 1000 * 3) {
            logger.error { "[알리미 스케줄] item check 시간 오래 걸림: $time ms" }
        }else{
            logger.info { "[알리미 스케줄] item check 시간 : $time ms"  }
        }
    }
}
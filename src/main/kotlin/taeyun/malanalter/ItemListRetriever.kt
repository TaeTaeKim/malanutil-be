package taeyun.malanalter

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import taeyun.malanalter.feignclient.ItemListClient
import taeyun.malanalter.repository.AlertRepository

private val logger = KotlinLogging.logger {  }
@Component
class ItemListRetriever(
    val alertRepository: AlertRepository,
    val itemListClient: ItemListClient,
) : ApplicationRunner {
    override fun run(args: ApplicationArguments?) {
        // 서버 시작 시 아이템 목록을 가져와서 저장
        try {
            val itemList = itemListClient.getAllItemList()
            itemList.forEach { item ->
                // 아이템 ID와 이름을 저장
                alertRepository.saveItemName(item.itemCode, item.itemName)
            }
            logger.info { "아이템 목록이 성공적으로 저장되었습니다. 로드된 아이템 : ${itemList.size}" }
        } catch (e: Exception) {
            logger.error { "메랜 지지에서 아이템 목록 로드 실패 ${e.message}" }
        }
    }
}
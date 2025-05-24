package taeyun.malanalter

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import taeyun.malanalter.feignclient.ItemListClient
import taeyun.malanalter.repository.CheckRepository

@Component
class ItemListRetriever(
    val checkRepository: CheckRepository,
    val itemListClient: ItemListClient,
) : ApplicationRunner {
    override fun run(args: ApplicationArguments?) {
        // 서버 시작 시 아이템 목록을 가져와서 저장
        val itemList = itemListClient.getAllItemList()
        itemList.forEach { item ->
            // 아이템 ID와 이름을 저장
            checkRepository.saveItemName(item.itemCode, item.itemName)
        }
        println("아이템 목록이 성공적으로 저장되었습니다." +  itemList.size)
    }
}
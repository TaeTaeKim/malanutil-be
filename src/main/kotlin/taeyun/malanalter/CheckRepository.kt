package taeyun.malanalter

import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

/**
 * 나중에는 DB로 변경해야 함
 */
@Component
class CheckRepository {
    val checkMap: ConcurrentHashMap<String, ItemCondition> = ConcurrentHashMap()
    companion object {
        val itemNameMap: ConcurrentHashMap<String, String> = ConcurrentHashMap()
    }

    fun getCheckItemIdAndPriceMap(): Map<String, ItemCondition> {
        // copy of map
        return checkMap.toMap()
    }
    fun save(itemId: String, itemCondition: ItemCondition) {
        checkMap[itemId] = itemCondition
    }

    fun delete(itemId: String) {
        checkMap.remove(itemId)
    }

    fun update(itemId: String, itemCondition: ItemCondition) {
        checkMap[itemId] = itemCondition
    }

    fun saveItemName(itemId: String, itemName: String) {
        itemNameMap[itemId] = itemName
    }

    fun getItemName(itemId: String): String {
        // 없으면 itemId 그대로 반환
        return itemNameMap[itemId] ?: itemId
    }
}
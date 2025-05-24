package taeyun.malanalter.repository

import org.springframework.stereotype.Component
import taeyun.malanalter.dto.ItemCondition
import taeyun.malanalter.dto.RegisteredItem
import java.util.concurrent.ConcurrentHashMap

/**
 * 나중에는 DB로 변경해야 함
 */
@Component
class CheckRepository {
    val checkMap: ConcurrentHashMap<Int, ItemCondition> = ConcurrentHashMap()
    companion object {
        val itemNameMap: ConcurrentHashMap<Int, String> = ConcurrentHashMap()
    }

    fun getRegisteredItem(): List<RegisteredItem> {
        // copy of map
        return checkMap.map { (itemId, itemCondition) ->
            RegisteredItem(
                itemId = itemId.toInt(),
                itemOptions = itemCondition,
                itemName = getItemName(itemId),
                isAlarm = true // 기본값으로 true 설정
            )
        }
    }
    fun save(itemId: Int, itemCondition: ItemCondition) {
        checkMap[itemId] = itemCondition
    }

    fun delete(itemId: Int) {
        checkMap.remove(itemId)
    }

    fun update(itemId: Int, itemCondition: ItemCondition) {
        checkMap[itemId] = itemCondition
    }

    fun saveItemName(itemId: Int, itemName: String) {
        itemNameMap[itemId] = itemName
    }

    fun getItemName(itemId: Int): String {
        // 없으면 itemId 그대로 반환
        return (itemNameMap[itemId] ?: itemId) as String
    }
}
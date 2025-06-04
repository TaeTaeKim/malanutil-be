package taeyun.malanalter.alertitem.repository

import taeyun.malanalter.alertitem.dto.ItemCondition
import taeyun.malanalter.alertitem.dto.RegisteredItem

interface AlertRepository {
    fun getRegisteredItem(): List<RegisteredItem>
    fun save(itemId: Int, itemCondition: ItemCondition)
    fun delete(alertId: Int)
    fun update(itemId: Int, itemCondition: ItemCondition)
    fun saveItemName(itemId: Int, itemName: String)
    fun getItemName(itemId: Int): String
    fun toggleItemAlarm(alertId: Int)
}
package taeyun.malanalter.repository

import taeyun.malanalter.dto.ItemCondition
import taeyun.malanalter.dto.RegisteredItem

interface AlertRepository {
    fun getRegisteredItem(): List<RegisteredItem>
    fun save(itemId: Int, itemCondition: ItemCondition)
    fun delete(itemId: Int)
    fun update(itemId: Int, itemCondition: ItemCondition)
    fun saveItemName(itemId: Int, itemName: String)
    fun getItemName(itemId: Int): String
}
package taeyun.malanalter.alertitem.repository

import taeyun.malanalter.alertitem.domain.AlertComment
import taeyun.malanalter.alertitem.dto.ItemBidInfo
import taeyun.malanalter.alertitem.dto.ItemCondition
import taeyun.malanalter.alertitem.dto.RegisteredItem

interface AlertRepository {
    fun getRegisteredItem(): List<RegisteredItem>
    fun save(itemId: Int, itemCondition: ItemCondition)
    fun delete(alertId: Int)
    fun update(alertId: Int, updateItemCondition: ItemCondition)
    fun saveItemName(itemId: Int, itemName: kotlin.String)
    fun toggleItemAlarm(alertId: Int)
    fun toggleAllItemAlarm(toggleTo:Boolean)
    fun getAllItemComments(): List<AlertComment>
    fun syncBids(itemId: Int, detectedBids: List<ItemBidInfo>, existBidList: List<AlertComment>)
}
package taeyun.malanalter.alertitem.dto

import taeyun.malanalter.alertitem.domain.AlertItemEntity
import taeyun.malanalter.alertitem.repository.AlertItemRepository

data class RegisteredItem (
    val id: Int,
    val itemId: Int,
    val itemOptions: ItemCondition,
    val itemName: String,
    val tradeType: TradeType,
    val isAlarm: Boolean
){
    constructor(alertItemEntity: AlertItemEntity) :this(
        id = alertItemEntity.id.value,
        itemId = alertItemEntity.itemId,
        itemOptions= alertItemEntity.itemCondition,
        tradeType = alertItemEntity.tradeType,
        itemName = AlertItemRepository.itemNameMap[alertItemEntity.itemId] ?: "이름 없음",
        isAlarm = alertItemEntity.isAlarm
    )
}
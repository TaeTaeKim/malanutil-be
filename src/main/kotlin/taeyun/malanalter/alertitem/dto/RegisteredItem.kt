package taeyun.malanalter.alertitem.dto

import taeyun.malanalter.alertitem.domain.AlertItemEntity
import taeyun.malanalter.alertitem.repository.AlertItemRepository

data class RegisteredItem (
    val id: Int,
    val itemId: Int,
    val itemOptions: ItemCondition,
    val itemName: String,
    val isAlarm: Boolean,
    val userId : Long
){
    constructor(alertItemEntity: AlertItemEntity) :this(
        id = alertItemEntity.id.value,
        itemId = alertItemEntity.itemId,
        itemOptions= alertItemEntity.itemCondition,
        itemName = AlertItemRepository.itemNameMap[alertItemEntity.itemId] ?: "이름 없음",
        isAlarm = alertItemEntity.isAlarm,
        userId = alertItemEntity.userId
    )
}
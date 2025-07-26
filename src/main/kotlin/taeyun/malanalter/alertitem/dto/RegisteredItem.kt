package taeyun.malanalter.alertitem.dto

import kotlinx.datetime.toJavaLocalDateTime
import taeyun.malanalter.alertitem.domain.AlertItemEntity
import taeyun.malanalter.alertitem.repository.AlertItemRepository
import java.time.ZoneOffset

data class RegisteredItem(
    val id: Int,
    val itemId: Int,
    val itemOptions: ItemCondition,
    val itemName: String,
    val tradeType: TradeType,
    val isAlarm: Boolean,
    val userId: Long,
    val createdAt: Long,
) {
    constructor(alertItemEntity: AlertItemEntity) : this(
        id = alertItemEntity.id.value,
        itemId = alertItemEntity.itemId,
        itemOptions = alertItemEntity.itemCondition,
        tradeType = alertItemEntity.tradeType,
        itemName = AlertItemRepository.itemNameMap[alertItemEntity.itemId] ?: "이름 없음",
        isAlarm = alertItemEntity.isAlarm,
        userId = alertItemEntity.userId,
        createdAt = alertItemEntity.createdAt.toJavaLocalDateTime().toEpochSecond(ZoneOffset.UTC),

    )
}
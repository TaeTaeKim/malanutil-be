package taeyun.malanalter.alertitem.repository

import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.inList
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import org.springframework.stereotype.Repository
import taeyun.malanalter.alertitem.domain.AlertItemEntity
import taeyun.malanalter.alertitem.domain.AlertItemTable
import taeyun.malanalter.alertitem.domain.ItemBidEntity
import taeyun.malanalter.alertitem.domain.ItemBidTable
import taeyun.malanalter.alertitem.dto.ItemBidInfo
import taeyun.malanalter.alertitem.dto.ItemCondition
import taeyun.malanalter.alertitem.dto.RegisteredItem
import taeyun.malanalter.alertitem.dto.TradeType
import taeyun.malanalter.user.UserService
import java.util.concurrent.ConcurrentHashMap

@Repository
class AlertItemRepository : AlertRepository {

    companion object {
        val itemNameMap: ConcurrentHashMap<Int, String> = ConcurrentHashMap()


        fun getItemName(itemId: Int): String {
            return itemNameMap[itemId] ?: "이름 없음"
        }

    }

    override fun getRegisteredItem(): List<RegisteredItem> = transaction {
        AlertItemEntity.all().map { RegisteredItem(it) }
    }

    override fun getAllItemComments(): List<ItemBidEntity> {
        return transaction {
            ItemBidEntity.all().toList()
        }
    }

    override fun syncBids(alertItemId: Int, detectedBids: List<ItemBidInfo>, existBidList: List<ItemBidEntity>)  {
        transaction {
            if (existBidList.isEmpty()) {
                bulkSaveFromBids(alertItemId, detectedBids)
            } else {
                val detectedBidIds = detectedBids.map { it.url }.toSet()
                val existingBidIds = existBidList.map { it.url }.toSet()

                val idsToRemove = existingBidIds - detectedBidIds
                if (idsToRemove.isNotEmpty()) {
                    ItemBidTable.deleteWhere { url inList idsToRemove }
                }

                val idsToAdd = detectedBidIds - existingBidIds
                val newBids = detectedBids.filter { it.url in idsToAdd }
                bulkSaveFromBids(alertItemId, newBids)
            }
        }
    }

    private fun bulkSaveFromBids(alertItemId: Int, bids: List<ItemBidInfo>) {
        ItemBidTable.batchInsert(bids) { bid ->
            this[ItemBidTable.url] = bid.url
            this[ItemBidTable.comment] = bid.comment
            this[ItemBidTable.alertItemId] = alertItemId
            this[ItemBidTable.price] = bid.itemPrice
        }
    }

    override fun save(itemId: Int, itemCondition: ItemCondition, tradeType: TradeType): Unit = transaction {
        AlertItemTable.insert {
            it[AlertItemTable.itemId] = itemId
            it[AlertItemTable.itemCondition] = itemCondition
            it[AlertItemTable.tradeType] = tradeType
            it[userId] = UserService.getLoginUserId()
        }
    }

    override fun delete(alertId: Int): Unit = transaction {
        AlertItemTable.deleteWhere { AlertItemTable.id eq alertId }
    }

    override fun update(alertId: Int, updateItemCondition: ItemCondition): Unit = transaction {

        // 현재 Exposed에서는 update 메서드를 직접 구현해야 함
        AlertItemTable.update(
            where = { AlertItemTable.id eq alertId }
        ) {
            it[itemCondition] = updateItemCondition
        }

        ItemBidTable.deleteWhere { alertItemId eq alertId }
    }

    override fun saveItemName(itemId: Int, itemName: String) {
        itemNameMap[itemId] = itemName
    }


    override fun toggleItemAlarm(alertId: Int) {
        transaction {
            AlertItemEntity.findByIdAndUpdate(alertId) {
                it.isAlarm = !it.isAlarm
            }
        }
    }

    override fun toggleAllItemAlarm(toggleTo: Boolean) {
        val loginUserId = UserService.getLoginUserId()
        transaction {
            AlertItemTable.update(
                where = { AlertItemTable.userId eq loginUserId }
            ) {
                it[isAalarm] = toggleTo
            }
        }
    }
}
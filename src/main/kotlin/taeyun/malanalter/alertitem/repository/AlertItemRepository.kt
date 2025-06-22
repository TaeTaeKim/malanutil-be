package taeyun.malanalter.alertitem.repository

import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.inList
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Repository
import taeyun.malanalter.alertitem.domain.AlertComment
import taeyun.malanalter.alertitem.domain.AlertComments
import taeyun.malanalter.alertitem.domain.AlertItemEntity
import taeyun.malanalter.alertitem.domain.AlertItemTable
import taeyun.malanalter.alertitem.dto.ItemBidInfo
import taeyun.malanalter.alertitem.dto.ItemCondition
import taeyun.malanalter.alertitem.dto.RegisteredItem
import taeyun.malanalter.user.UserService
import java.util.concurrent.ConcurrentHashMap

@Suppress("UNREACHABLE_CODE")
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

    override fun getAllItemComments(): List<AlertComment> {
        return transaction {
            AlertComment.all().toList()
        }
    }

    override fun syncBids(alertItemId: Int, detectedBids: List<ItemBidInfo>, existBidList: List<AlertComment>)  {
        transaction {
            if (existBidList.isEmpty()) {
                AlertComments.batchInsert(detectedBids) { bid ->
                    this[AlertComments.id] = bid.id
                    this[AlertComments.comment] = bid.comment
                    this[AlertComments.alertItemId] = alertItemId
                    this[AlertComments.isAlarm] = true
                }
            } else {
                val detectedBidIds = detectedBids.map { it.id }.toSet()
                val existingBidIds = existBidList.map { it.id.value }

                val idsToRemove = existingBidIds - detectedBidIds;
                if (idsToRemove.isNotEmpty()) {
                    AlertComments.deleteWhere { id inList idsToRemove }
                }

                val idsToAdd = detectedBidIds - existingBidIds
                val newBids = detectedBids.filter { it.id in idsToAdd }
                AlertComments.batchInsert(newBids) { bid ->
                    this[AlertComments.id] = bid.id
                    this[AlertComments.alertItemId] = alertItemId
                    this[AlertComments.isAlarm] = true
                }
            }
        }
    }

    override fun save(itemId: Int, itemCondition: ItemCondition): Unit = transaction {
        AlertItemTable.insert {
            it[AlertItemTable.itemId] = itemId
            it[AlertItemTable.itemCondition] = itemCondition
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

        // todo: 해당 아이템의 코멘트 리스트를 모두 제거해야한다.
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
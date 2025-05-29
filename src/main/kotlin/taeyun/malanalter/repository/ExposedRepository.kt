package taeyun.malanalter.repository

import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import org.springframework.stereotype.Repository
import taeyun.malanalter.dto.ItemCondition
import taeyun.malanalter.dto.RegisteredItem
import taeyun.malanalter.schema.AlertItemTable
import java.util.concurrent.ConcurrentHashMap

@Suppress("UNREACHABLE_CODE")
@Repository
class ExposedRepository : AlertRepository {

    companion object {
        val itemNameMap: ConcurrentHashMap<Int, String> = ConcurrentHashMap()
    }

    override fun getRegisteredItem(): List<RegisteredItem> = transaction {
        AlertItemTable.selectAll().map { row -> RegisteredItem(row) }
    }

    override fun save(itemId: Int, itemCondition: ItemCondition): Unit = transaction {
        AlertItemTable.insert {
            it[AlertItemTable.itemId] = itemId
            it[AlertItemTable.itemCondition] = itemCondition
        }
    }

    override fun delete(itemId: Int): Unit = transaction {
        AlertItemTable.deleteWhere { AlertItemTable.itemId eq itemId }
    }

    override fun update(itemId: Int, itemCondition: ItemCondition): Unit = transaction {
        // 현재 Exposed에서는 update 메서드를 직접 구현해야 함
        AlertItemTable.update(
            where = { AlertItemTable.itemId eq itemId }
        ) {
            it[AlertItemTable.itemCondition] = itemCondition
        }
    }

    override fun saveItemName(itemId: Int, itemName: String) {
        itemNameMap[itemId] = itemName
    }

    override fun getItemName(itemId: Int): String {
        return itemNameMap[itemId] ?: "이름 없음"
    }


}
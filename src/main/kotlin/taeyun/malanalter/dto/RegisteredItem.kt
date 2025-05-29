package taeyun.malanalter.dto

import org.jetbrains.exposed.v1.core.ResultRow
import taeyun.malanalter.repository.ExposedRepository
import taeyun.malanalter.schema.AlertItemTable

data class RegisteredItem (
    val itemId: Int,
    val itemOptions: ItemCondition,
    val itemName: String,
    val isAlarm: Boolean
){
    constructor(row : ResultRow) : this(
        itemId = row[AlertItemTable.itemId].toInt(),
        itemOptions = ItemCondition(
            price = row[AlertItemTable.itemCondition].price,
            str = row[AlertItemTable.itemCondition].str,
            dex = row[AlertItemTable.itemCondition].dex,
            int = row[AlertItemTable.itemCondition].int,
            luk = row[AlertItemTable.itemCondition].luk,
            pad = row[AlertItemTable.itemCondition].pad,
            mad = row[AlertItemTable.itemCondition].mad,
            hapma = row[AlertItemTable.itemCondition].hapma,
            accuracy = row[AlertItemTable.itemCondition].accuracy,
            speed = row[AlertItemTable.itemCondition].speed
        ),
        itemName = ExposedRepository.itemNameMap[row[AlertItemTable.itemId]] ?: "이름 없음",
        isAlarm = row[AlertItemTable.isAalarm]
    )
}
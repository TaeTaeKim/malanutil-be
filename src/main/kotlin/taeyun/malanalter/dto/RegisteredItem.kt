package taeyun.malanalter.dto

data class RegisteredItem (
    val itemId: Int,
    val itemOptions: ItemCondition,
    val itemName: String,
    val isAlarm: Boolean
)
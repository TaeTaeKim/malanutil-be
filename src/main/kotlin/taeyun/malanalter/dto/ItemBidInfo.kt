package taeyun.malanalter.dto

import com.fasterxml.jackson.annotation.JsonCreator


data class ItemBidInfo (
    val tradeStatus: Boolean,
    val itemPrice: Int,
    val comment: String,
    val itemName: String,
    val tradeType: TradeType
){
    enum class TradeType {
        BUY, SELL;

        companion object {
            @JvmStatic
            @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
            fun fromString(value: String): TradeType {
                return when (value) {
                    "buy" -> BUY
                    "sell" -> SELL
                    else -> throw IllegalArgumentException("Invalid value $value")
                }
            }
        }
    }
    fun toDiscordMessage(): String {
        return "가격=$itemPrice, comment=$comment"
    }

}


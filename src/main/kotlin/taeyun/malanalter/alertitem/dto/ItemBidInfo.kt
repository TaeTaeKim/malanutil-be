package taeyun.malanalter.alertitem.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty


data class ItemBidInfo (
    val tradeStatus: Boolean,
    val itemPrice: Long,
    val comment: String,
    val itemName: String,
    val tradeType: TradeType,
    @JsonProperty("url")
    val id: String,
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
        return "- 가격=${ItemCondition.changePriceToString(itemPrice)}, comment=$comment  | [링크](https://mapleland.gg/trade/$id)"
    }

}


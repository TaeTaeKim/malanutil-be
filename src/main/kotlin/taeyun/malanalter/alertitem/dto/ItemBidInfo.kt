package taeyun.malanalter.alertitem.dto

import com.fasterxml.jackson.annotation.JsonProperty


data class ItemBidInfo (
    val tradeStatus: Boolean,
    val itemPrice: Long,
    val comment: String?="",
    val itemName: String,
    val tradeType: TradeType,
    @JsonProperty("url")
    val url: String,
){
    fun toDiscordMessage(): String {
        return ">[${tradeType.toKorean()}] 가격=${ItemCondition.changePriceToString(itemPrice)}, comment=${comment}  | [링크](https://mapleland.gg/trade/$url)"
    }

}


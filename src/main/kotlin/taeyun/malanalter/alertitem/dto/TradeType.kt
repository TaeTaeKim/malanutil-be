package taeyun.malanalter.alertitem.dto

import com.fasterxml.jackson.annotation.JsonCreator

/**
 * BUY : 팝니다 매물 검색 아이템 (메랜지지 기준)
 * SELL : 삽니다 매물 검색 아이템
 */
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

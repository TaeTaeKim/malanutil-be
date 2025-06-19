package taeyun.malanalter.alertitem.dto

import kotlinx.serialization.Serializable

/**
 * Malan gg 로 보내는 쿼리 파라미터
 */
@Serializable
data class ItemCondition(
    val lowPrice: Int? = null,
    val highPrice: Int? = null,
    val str: Int? = null,
    val dex: Int? = null,
    val int: Int? = null,
    val luk: Int? = null,
    val pad: Int? = null,  // 공격력
    val mad: Int? = null, // 마력
    val hapma: Int? = null, // 합마
    val accuracy: Int? = null, // 명중률
    val speed: Int? = null, // 이동속도
    val jump: Int? = null, // 점프력
){
    fun makeRegisterOptionMsg():List<String>{
        return buildList {
            str?.let { add("힘: $it, ") }
            dex?.let { add("덱스: $it, ") }
            int?.let { add("인트: $it, ") }
            luk?.let { add("럭: $it, ") }
            pad?.let { add("공격력: $it, ") }
            mad?.let { add("마력: $it, ") }
            hapma?.let { add("합마: $it, ") }
            accuracy?.let { add("명중률: $it, ") }
            speed?.let { add("이속: $it, ") }
            jump?.let { add("점프력: $it, ") }
        }
    }

    fun getStringPrice(priceType: String): String {
        return when (priceType) {
            "low" -> changePriceToString(lowPrice)
            "high" -> changePriceToString(highPrice)
            else -> throw IllegalArgumentException("priceType $priceType is not valid, should be one of [low, high]")
        }
    }

    companion object{
        fun changePriceToString(price: Int?): String {
            return when {
                price == null -> "0"
                price >=100000000 -> String.format("%.2f", price.toDouble() / 100000000) + "억"
                price >= 1000000 -> "${price/10000}만"
                else -> price.toString()
            }
        }
        fun changePriceToString(price: Long?): String {
            return when {
                price == null -> "0"
                price >=100000000 -> String.format("%.2f", price.toDouble() / 100000000) + "억"
                price >= 1000000 -> "${price/10000}만"
                else -> price.toString()
            }
        }

    }
}
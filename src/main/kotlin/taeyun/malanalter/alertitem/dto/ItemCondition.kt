package taeyun.malanalter.alertitem.dto

import kotlinx.serialization.Serializable

/**
 * Malan gg 로 보내는 쿼리 파라미터
 */
@Serializable
data class ItemCondition(
    val lowPrice: Long? = null,
    val highPrice: Long? = null,
    val str: Int? = null,
    val highSTR: Int? = null,
    val dex: Int? = null,
    val highDEX: Int? = null,
    val int: Int? = null,
    val highINT: Int? = null,
    val luk: Int? = null,
    val highLUK: Int? = null,
    val pad: Int? = null,  // 공격력
    val highPAD: Int? = null, // 최고 공격력
    val mad: Int? = null, // 마력
    val highMAD: Int? = null, // 최고 마력
    val hapma: Int? = null, // 합마
    val highHAPMA: Int? = null, // 최고 합마
    val accuracy: Int? = null, // 명중률
    val highACCURACY: Int? = null, // 최고 명중률
    val speed: Int? = null, // 이동속도
    val highSPEED: Int? = null, // 최고 이동속도
    val jump: Int? = null, // 점프력
    val highJUMP: Int? = null, // 최고 점프력
    val upgrade: Int? = null, // 강화
    val highUPGRADE: Int? = null, // 최고 강화
    // 추가 옵션
    val pdd: Int? = null, // 물리방어력
    val highPDD: Int? = null,
    val mdd: Int? = null, // 마법 방어력
    val highMDD: Int? = null,
    val eva: Int? = null,
    val highEVA: Int? = null,
    val mhp: Int? = null,
    val highMHP: Int? = null,
    val mmp: Int? = null,
    val highMMP: Int? = null,
    // 합스탯
    val hapStats: List<String> = emptyList(),
    val combinedStat: Int? = null,
    val highCOMBINEDSTAT: Int? = null,

    ) {
    // 아이템 등록시 메세지에 추가되는 옵션 메세지 생성함수
    fun makeRegisterOptionMsg(): List<String> {
        return buildList {
            if (str != null || highSTR != null) {
                add("힘: ${str ?: 0}~${highSTR ?: str ?: 0}, ")
            }
            if (dex != null || highDEX != null) {
                add("덱스: ${dex ?: 0}~${highDEX ?: dex ?: 0}, ")
            }
            if (int != null || highINT != null) {
                add("인트: ${int ?: 0}~${highINT ?: int ?: 0}, ")
            }
            if (luk != null || highLUK != null) {
                add("럭: ${luk ?: 0}~${highLUK ?: luk ?: 0}, ")
            }
            if (pad != null || highPAD != null) {
                add("공격력: ${pad ?: 0}~${highPAD ?: pad ?: 0}, ")
            }
            if (mad != null || highMAD != null) {
                add("마력: ${mad ?: 0}~${highMAD ?: mad ?: 0}, ")
            }
            if (hapma != null || highHAPMA != null) {
                add("합마: ${hapma ?: 0}~${highHAPMA ?: hapma ?: 0}, ")
            }
            if (accuracy != null || highACCURACY != null) {
                add("명중률: ${accuracy ?: 0}~${highACCURACY ?: accuracy ?: 0}, ")
            }
            if (speed != null || highSPEED != null) {
                add("이속: ${speed ?: 0}~${highSPEED ?: speed ?: 0}, ")
            }
            if (jump != null || highJUMP != null) {
                add("점프력: ${jump ?: 0}~${highJUMP ?: jump ?: 0}, ")
            }
            if (upgrade != null || highUPGRADE != null) {
                add("작횟: ${upgrade ?: 0}~${highUPGRADE ?: upgrade ?: 0}, ")
            }
            if (pad != null || highPAD != null) {
                add("물방 : ${pdd ?: 0}~${highPDD ?: pdd ?: 0}, ")
            }
            if (mad != null || highMAD != null) {
                add("마방 : ${mdd ?: 0}~${highMDD ?: mdd ?: 0}, ")
            }
            if (eva != null || highEVA != null) {
                add("회피 : ${eva ?: 0}~${highEVA ?: eva ?: 0}, ")
            }
            if (mhp != null || highMHP != null) {
                add("HP : ${mhp ?: 0}~${highMHP ?: mhp ?: 0}, ")
            }
            if (mmp != null || highMMP != null) {
                add("MP : ${mmp ?: 0}~${highMMP ?: mmp ?: 0}, ")
            }
            // 합스탯 메세지
            if (hapStats.isNotEmpty() && combinedStat != null) {
                add("합스탯(${hapStats.joinToString(", ")}): ${combinedStat}~${highCOMBINEDSTAT ?: combinedStat}, ")
            }
        }
    }

    
    fun makeHapStatName(): String? {
        // Define the canonical order for stats
        val statOrder = listOf("STR", "DEX", "INT", "LUK", "ACC")

        return hapStats
            .takeIf { it.isNotEmpty() }
            ?.map { it.uppercase() }  // Convert to uppercase
            ?.sortedBy { statOrder.indexOf(it) }  // Sort by predefined order
            ?.joinToString("")  // Join without separator
    }

    fun getStringPrice(priceType: String): String {
        return when (priceType) {
            "low" -> changePriceToString(lowPrice)
            "high" -> changePriceToString(highPrice)
            else -> throw IllegalArgumentException("priceType $priceType is not valid, should be one of [low, high]")
        }
    }

    companion object {
        fun changePriceToString(price: Long?): String {
            return when {
                price == null -> "0"
                price >= 100000000 -> String.format("%.2f", price.toDouble() / 100000000) + "억"
                price >= 1000000 -> "${price / 10000}만"
                else -> price.toString()
            }
        }

    }
}
package taeyun.malanalter.alertitem.dto

import kotlinx.serialization.Serializable

/**
 * Malan gg 로 보내는 쿼리 파라미터
 */
@Serializable
data class ItemCondition(
    val price: Int? = null,
    val str: Int? = null,
    val dex: Int? = null,
    val int: Int? = null,
    val luk: Int? = null,
    val pad: Int? = null,
    val mad: Int? = null,
    val hapma: Int? = null, // 합마
    val accuracy: Int? = null, // 명중률
    val speed: Int? = null, // 이동속도
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
        }
    }
}
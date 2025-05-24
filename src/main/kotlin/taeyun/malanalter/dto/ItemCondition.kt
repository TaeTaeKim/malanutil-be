package taeyun.malanalter.dto

/**
 * Malan gg 로 보내는 쿼리 파라미터
 */
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
) {
}
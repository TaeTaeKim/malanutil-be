package taeyun.malanalter

/**
 * Malan gg 로 보내는 쿼리 파라미터
 */
data class ItemCondition(
    val lowPrice: Int? = null,
    val highPrice: Int? = null,
    val lowincINT: Int? = null,
    val highincINT: Int? = null,
    val lowincSTR: Int? = null,
    val highincSTR: Int? = null,
    val lowincDEX: Int? = null,
    val highincDEX: Int? = null,
    val lowincLUK: Int? = null,
    val highincLUK: Int? = null,
    val lowincPAD: Int? = null,
    val highincPAD: Int? = null,
    val lowincMAD: Int? = null,
    val highincMAD: Int? = null
) {
}
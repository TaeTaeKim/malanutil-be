package taeyun.malanalter.alertitem.dto

data class DiscordMessage (
    val content: String? = null
){
    constructor(bids: List<ItemBidInfo>) : this(
        content = if (bids.isNotEmpty()) {
            val itemName = bids[0].itemName
            val sb = StringBuilder()
            sb.append("```")
            sb.append("아이템 이름: $itemName\n")
            bids.forEach {
                sb.append(it.toDiscordMessage())
                sb.append("\n")
            }
            sb.append("```")
            sb.toString()
        } else null
    )

    companion object{
        fun testDiscordMessage():DiscordMessage{
            return DiscordMessage("테스트 메세지 입니다.")
        }
    }
}
package taeyun.malanalter

class DiscordMessage {
    var content: String? = null

    companion object {
        fun create(bids: List<ItemBidInfo>): DiscordMessage {
            val discordMessage = DiscordMessage()
            if (bids.isNotEmpty()) {
                val itemName = bids[0].itemName
                val sb = StringBuilder()
                sb.append("```")
                sb.append("아이템 이름: $itemName\n")
                bids.forEach {
                    sb.append(it.toDiscordMessage())
                    sb.append("\n")
                }
                sb.append("```")
                discordMessage.content = sb.toString()
            }
            return discordMessage
        }
    }
}
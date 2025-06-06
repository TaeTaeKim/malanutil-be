package taeyun.malanalter.alertitem.dto

data class DiscordMessage(
    val catchBids: MutableMap<Int, List<ItemBidInfo>> = mutableMapOf()
) {

    fun addBids(alertId: Int, bidsList: List<ItemBidInfo>) {
        if (bidsList.isEmpty()) return
        catchBids[alertId] = bidsList
    }


    fun getDiscordMessageContents(): List<String> = buildList {
        // iterate catchedBids map
        catchBids.entries.chunked(3)
            // 청크 순회
            .forEach { chunk ->
                // 각 청크별로 MessageContent 생성
                add(buildString {
                    chunk.forEach { (_, bids) ->
                        append("### ${getItemName(bids)} 지지알림\n")
                        append("```\n")
                        bids.take(5).forEach { bid ->
                            append(bid.toDiscordMessage())
                            append("\n")
                        }
                        append("```\n")
                    }
                })
            }
    }
    companion object{
        fun testDiscordMessage(): String{
            return "톡톡🎤 매랜지지 알리미가 보내는 테스트 메세지 입니다"
        }

        fun welcomeMessage(): String{
            // todo: 웰컴메세지에 추가 작성
            return "메랜지지 알리미에 오신 걸 환영합니다. ... 링크"
        }
    }



}

private fun getItemName(bidsList: List<ItemBidInfo>): String = bidsList.first().itemName
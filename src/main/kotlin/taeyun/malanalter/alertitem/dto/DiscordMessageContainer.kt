package taeyun.malanalter.alertitem.dto

import taeyun.malanalter.alertitem.repository.AlertItemRepository

data class DiscordMessageContainer(
    val bidsToBeSent: MutableMap<Int, List<ItemBidInfo>> = mutableMapOf()
) {

    fun addBids(alertItemId: Int, bidsList: List<ItemBidInfo>) {
        if (bidsList.isEmpty()) return
        bidsToBeSent[alertItemId] = bidsList
    }


    // 한 메세지에 2000자를 넘으면 안되기에 아이템 3개씩 분할해서 메세지 생성
    // 아이템 3개에 + 아이템당 5개의 코멘트로 메세지를 만든다.
    fun getMessageContentList(): List<String> = buildList {
        // iterate catchedBids map
        if (bidsToBeSent.isEmpty()) {
            return emptyList()
        }
        bidsToBeSent.entries.chunked(3)
            // 청크 순회
            .forEach { chunk ->
                // 각 청크별로 MessageContent 생성
                add(buildString {
                    chunk.forEach { (_, bids) ->
                        append("### ${getItemName(bids)} [알리미 홈페이지](https://malanutil.com/malan-alerter)\n")
                        bids.forEach { bid ->
                            append(bid.toDiscordMessage())
                            append("\n")
                        }
                        append("\n")
                    }
                })
            }
    }

    companion object {
        fun testDiscordMessage(): String {
            return "톡톡🎤 매랜지지 알리미가 보내는 테스트 메세지 입니다"
        }

        fun welcomeMessage(): String {
            return """
                
                안녕하세요! 👋
                _**메랜 유틸**_ 에 오신 걸 환영합니다.
                
                이 봇은 메랜지지에 올라온 매물 중, 
                **조건에 맞는 매물을 5분마다 확인해서 DM으로** 알려주고 있습니다

                메랜 유틸은 아래 서비스를 운영중입니다!
                - 👉 메랜지지 **매물 알리 서비스** : https://malanutil.com/malan-alerter 
                - 👉 메랜 **사냥 타이머** : https://malanutil.com/malan-timer

                오류나 건의사항은 디스코드 내 <이슈> 채널에 남겨주시면 빠르게 확인하겠습니다!

                감사합니다.
            """.trimIndent()
        }

        fun alertItemRegisterMessage(itemId: Int, itemCondition: ItemCondition, tradeType: TradeType) = buildString {
            append("## 아이템 등록알림\n")
            val itemName = AlertItemRepository.getItemName(itemId)
            append("**[$itemName]** 등록완료. **${itemCondition.getStringPrice("low")} ~ ${itemCondition.getStringPrice("high")}메소** 로 [${tradeType.toKorean()}] 매물이 나오면 알려드릴게요\n\n")

            val conditions = itemCondition.makeRegisterOptionMsg()
            if (conditions.isNotEmpty()) {
                append("**옵션** ")
                conditions.forEach{append(it)}
            }
        }
    }
}

private fun getItemName(bidsList: List<ItemBidInfo>): String = bidsList.first().itemName
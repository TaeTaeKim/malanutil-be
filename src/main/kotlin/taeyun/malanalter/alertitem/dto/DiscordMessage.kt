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
            return """
                안녕하세요! 👋
                메랜지지 알리미에 오신 걸 환영합니다.

                이 봇은 메랜지지에 올라온 아이템 중, 설정한 조건에 맞는 글을 5분마다 확인해서 DM으로 알려주는 서비스입니다.

                바로 시작하려면 아래 링크로 이동해서 원하는 조건을 등록해주세요.
                👉 https://malanutil.com/malan-alerter

                오류나 건의사항은 디스코드 내 <이슈> 채널에 남겨주시면 빠르게 확인하겠습니다!

                감사합니다.
            """.trimIndent()
        }
    }



}

private fun getItemName(bidsList: List<ItemBidInfo>): String = bidsList.first().itemName
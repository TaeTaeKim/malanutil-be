package taeyun.malanalter

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import taeyun.malanalter.alertitem.domain.ItemBidEntity
import taeyun.malanalter.alertitem.dto.ItemBidInfo
import taeyun.malanalter.alertitem.dto.ItemCondition
import taeyun.malanalter.alertitem.dto.MalanggBidRequest
import taeyun.malanalter.alertitem.dto.RegisteredItem
import taeyun.malanalter.alertitem.repository.AlertRepository
import taeyun.malanalter.feignclient.MalanClient

class ItemCheckerTest : StringSpec({

    val repo  = mockk<AlertRepository>()
    val client = mockk<MalanClient>()

    val checker = ItemChecker(repo, client, mockk(), mockk())


    "메랜 지지 응답에서 유저가 off 한 comment(bid) 가 있으면 제외하고 가격 오름차순 5개를 보여준다."{
        val item = RegisteredItem(
            id = 1,
            userId = 100L,
            itemName = "testItemName",
            itemId = 10,
            itemOptions = ItemCondition(),
            isAlarm = true
        )

        // 기존에 존재하던
        val existBids = listOf(
            mockk<ItemBidEntity>{
                every { id.value } returns "bid-3"
                every { isAlarm } returns false
            }
        )

        val responseBids = listOf(
            ItemBidInfo(true, 600L, "comment6", "testItemName",ItemBidInfo.TradeType.SELL, "bid-6"),
            ItemBidInfo(true, 200L, "comment2", "testItemName",ItemBidInfo.TradeType.SELL, "bid-2"),
            ItemBidInfo(true, 100L, "comment1", "testItemName",ItemBidInfo.TradeType.SELL, "bid-1"),
            ItemBidInfo(true, 300L, "comment3", "testItemName",ItemBidInfo.TradeType.SELL, "bid-3"),
            ItemBidInfo(true, 400L, "comment4", "testItemName",ItemBidInfo.TradeType.SELL, "bid-4"),
            ItemBidInfo(true, 500L, "comment5", "testItemName",ItemBidInfo.TradeType.SELL, "bid-5"),
            ItemBidInfo(true, 700L, "comment7", "testItemName",ItemBidInfo.TradeType.SELL, "bid-7"),

        )
        every { client.getItemBidList(item.itemId, any<MalanggBidRequest>()) } returns responseBids
        justRun { repo.syncBids(item.id, any(), any()) }

        val result = checker.requestItemBids(item, existBids)

        // then
        result.size shouldBe 5
        result.map { it.id } shouldBe listOf("bid-1", "bid-2", "bid-4", "bid-5", "bid-6")
    }


})

package taeyun.malanalter

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import taeyun.malanalter.alertitem.domain.ItemBidEntity
import taeyun.malanalter.alertitem.dto.*
import taeyun.malanalter.alertitem.repository.AlertRepository
import taeyun.malanalter.feignclient.MalanClient

class ItemCheckerTest : StringSpec({

    val repo = mockk<AlertRepository>()
    val client = mockk<MalanClient>()

    val checker = ItemCheckerV2(repo, client, mockk(), mockk())

    val sellBids = listOf(
        ItemBidInfo(true, 600L, "comment6", "testItemName", TradeType.SELL, "bid-6"),
        ItemBidInfo(true, 200L, "comment2", "testItemName", TradeType.SELL, "bid-2"),
        ItemBidInfo(true, 100L, "comment1", "testItemName", TradeType.SELL, "bid-1"),
        ItemBidInfo(true, 300L, "comment3", "testItemName", TradeType.SELL, "bid-3"),
        ItemBidInfo(true, 400L, "comment4", "testItemName", TradeType.SELL, "bid-4"),
        ItemBidInfo(true, 500L, "comment5", "testItemName", TradeType.SELL, "bid-5"),
        ItemBidInfo(true, 700L, "comment7", "testItemName", TradeType.SELL, "bid-7"),
    )
    val buyBids = listOf(
        ItemBidInfo(true, 800L, "comment8", "testItemName", TradeType.BUY, "bid-8"),
        ItemBidInfo(true, 900L, "comment9", "testItemName", TradeType.BUY, "bid-9"),
        ItemBidInfo(true, 1000L, "comment10", "testItemName", TradeType.BUY, "bid-10"),
        ItemBidInfo(true, 1100L, "comment11", "testItemName", TradeType.BUY, "bid-11"),
        ItemBidInfo(true, 1200L, "comment12", "testItemName", TradeType.BUY, "bid-12"),
        ItemBidInfo(true, 1300L, "comment13", "testItemName", TradeType.BUY, "bid-13"),
    )
    val item = RegisteredItem(
        id = 1,
        userId = 100L,
        itemName = "testItemName",
        itemId = 10,
        tradeType = TradeType.SELL,
        itemOptions = ItemCondition(),
        isAlarm = true,
        createdAt = 0L
    )

    "메랜 지지 응답에서 유저가 off 한 comment(bid) 가 있으면 제외하고 가격 오름차순 5개를 보여준다." {
        // 기존에 존재하던
        val existBids = listOf(
            mockk<ItemBidEntity> {
                every { url } returns "bid-3"
                every { isAlarm } returns false
            }
        )
        every { client.getItemBidList(item.itemId, any<MalanggBidRequest>()) } returns sellBids
        justRun { repo.syncBids(item.id, any(), any()) }

        val result = checker.requestItemBids(item, existBids)

        // then
        result.size shouldBe 5
        result.map { it.url } shouldBe listOf("bid-1", "bid-2", "bid-4", "bid-5", "bid-6")
    }
    "삽니다는 가격이 높은순으로 정렬" {
        val existBids = emptyList<ItemBidEntity>()
        every { client.getItemBidList(item.itemId, any<MalanggBidRequest>()) } returns buyBids
        justRun { repo.syncBids(item.id, any(), any()) }

        val result = checker.requestItemBids(item.copy(tradeType = TradeType.BUY), existBids)

        // then
        result.size shouldBe 5
        result.map { it.url } shouldBe listOf("bid-13", "bid-12", "bid-11", "bid-10", "bid-9")
    }

    "이미 보낸 알람은 보내지 않는다" {
        val existBids = listOf(
            mockk<ItemBidEntity> {
                every { url } returns "bid-1"
                every { isAlarm } returns true
                every { isSent } returns true
            }
        )
        every { client.getItemBidList(item.itemId, any<MalanggBidRequest>()) } returns sellBids
        justRun { repo.syncBids(item.id, any(), any()) }
        val result = checker.requestItemBids(item, existBids)
        // then
        result.size shouldBe 4
        result.map { it.url } shouldBe listOf("bid-2", "bid-3", "bid-4", "bid-5")

    }

    "우선순위가 높은 알림이 등장하면 보내야한다. -> 기존은 보내지 않는다." {
        // sell bid exist from 2
        val existBids = listOf(
            mockk<ItemBidEntity> {
                every { url } returns "bid-2"
                every { isAlarm } returns true
                every { isSent } returns true
            },
            mockk<ItemBidEntity> {
                every { url } returns "bid-3"
                every { isAlarm } returns true
                every { isSent } returns true
            }
        )
        every { client.getItemBidList(item.itemId, any<MalanggBidRequest>()) } returns sellBids
        justRun { repo.syncBids(item.id, any(), any()) }

        // 2, 3번 비드가 이미 존재하고 알람이 켜져있고 보낸적이 있다. -> 1, 4, 5 비드가 오름차순으로 정렬되어서 보여져야한다
        // 2,3 이 보내졌지만 알람은 켜져있기 때문에 6은 보내지 않는다. 즉 1,4,5 알림 보낸다.
        val result = checker.requestItemBids(item, existBids)
        // then
        result.size shouldBe 3
        result.map { it.url } shouldBe listOf("bid-1", "bid-4", "bid-5")
    }

    "알람을 끄면 이후 상위 5개 비드 중 보내지 않은 알람을 보낸다." {
        val existBids = listOf(
            mockk<ItemBidEntity> {
                every { url } returns "bid-2"
                every { isAlarm } returns false
                every { isSent } returns true
            },
            mockk<ItemBidEntity> {
                every { url } returns "bid-3"
                every { isAlarm } returns true
                every { isSent } returns true
            }
        )

        every { client.getItemBidList(item.itemId, any<MalanggBidRequest>()) } returns sellBids
        justRun { repo.syncBids(item.id, any(), any()) }

        // 2, 3번 비드가 이미 존재하고 알람이 켜져있고 보낸적이 있다. -> 2번이 꺼져있다.
        // 1,4,5,6 비드가 오름차순으로 정렬되어서 보여져야한다
        val result = checker.requestItemBids(item, existBids)
        // then
        result.size shouldBe 4
        result.map { it.url } shouldBe listOf("bid-1", "bid-4", "bid-5", "bid-6")

    }


})

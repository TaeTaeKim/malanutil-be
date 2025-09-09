package taeyun.malanalter.alertitem.repository

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalTime
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.context.annotation.Import
import taeyun.malanalter.ExposedTest
import taeyun.malanalter.alertitem.domain.AlertItemTable
import taeyun.malanalter.alertitem.domain.ItemBidEntity
import taeyun.malanalter.alertitem.domain.ItemBidTable
import taeyun.malanalter.alertitem.dto.ItemBidInfo
import taeyun.malanalter.alertitem.dto.ItemCondition
import taeyun.malanalter.alertitem.dto.TradeType
import taeyun.malanalter.user.domain.Users

@ExposedTest
@Import(AlertItemRepository::class)
class AlertItemRepositoryTest(
    val alertItemRepository: AlertItemRepository
) : StringSpec({

    val userId = 123L
    val alertId = 1

    beforeSpec {
        transaction {
            Users.insert {
                it[id] = userId
                it[username] = "testUsername"
                it[startTime] = LocalTime(0, 30)
                it[endTime] = LocalTime(13, 30)
            }
            AlertItemTable.insert {
                it[AlertItemTable.id] = alertId
                it[itemId] = 12345
                it[tradeType] = TradeType.SELL
                it[itemCondition] = ItemCondition()
                it[AlertItemTable.userId] = userId
            }
        }
    }

    beforeTest {
        transaction {
            ItemBidTable.deleteAll()
        }
    }

    "아이템에 대한 bid list 가 없으면 bulk insert" {

        val detectedBids = listOf(
            ItemBidInfo(true, 100L, "comment1", "testName1", TradeType.SELL, "testUrl1"),
            ItemBidInfo(true, 200L, "comment2", "testName2", TradeType.SELL, "testUrl2")
        )
        val emptyExistBids = emptyList<ItemBidEntity>()

        alertItemRepository.syncBids(alertId, detectedBids, emptyExistBids)

        //then
        transaction {
            val savedBids = ItemBidEntity.all().toList()
            savedBids.size shouldBe 2
            savedBids.map { it.url }.toSet() shouldBe setOf("testUrl1", "testUrl2")
            savedBids.all { it.alertItemId.value == alertId } shouldBe true
        }


    }

    "아이템에 대한 bid가 있을때는 sync를 맞춘다." {

        transaction {
            ItemBidTable.insert {
                it[url] = "testUrl1"
                it[price] = 1000
                it[alertItemId] = alertId
                it[isAlarm] = true
            }
        }

        // testUrl1은 삭제된 상황
        val detectedBids = listOf(
            ItemBidInfo(true, 200L, "comment2", "testName2", TradeType.SELL, "testUrl2")
        )

        val existBids: List<ItemBidEntity> = transaction {
            ItemBidEntity.find { ItemBidTable.alertItemId eq alertId }.toList()
        }

        alertItemRepository.syncBids(alertId, detectedBids, existBids)

        //then
        transaction {
            val remainBis = ItemBidEntity.all().toList()
            remainBis.size shouldBe 1
            val get = remainBis.get(0)
            get.url shouldBe "testUrl2"
        }
    }


})

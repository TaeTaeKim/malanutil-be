package taeyun.malanalter

import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.context.ActiveProfiles
import taeyun.malanalter.alertitem.dto.ItemCondition
import taeyun.malanalter.alertitem.dto.RegisteredItem
import taeyun.malanalter.alertitem.dto.TradeType
import taeyun.malanalter.alertitem.repository.AlertRepository
import taeyun.malanalter.auth.discord.DiscordService
import taeyun.malanalter.feignclient.MalanClient
import taeyun.malanalter.user.UserService
import taeyun.malanalter.user.domain.UserEntity
import kotlin.system.measureTimeMillis

@ActiveProfiles("test")
class ItemCheckerPerformanceTest {

    @MockK
    private lateinit var alertRepository: AlertRepository

    @MockK
    private lateinit var malanClient: MalanClient

    @MockK
    private lateinit var userService: UserService

    @MockK
    private lateinit var discordService: DiscordService

    private lateinit var asyncChecker: ItemCheckerV2

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        asyncChecker = ItemCheckerV2(alertRepository, malanClient, userService, discordService)

        // Mock UserEntity objects
        val user1 = mockk<UserEntity>()
        every { user1.isAlarmOff() } returns false
        every { user1.isNotAlarmTime() } returns false

        val user2 = mockk<UserEntity>()
        every { user2.isAlarmOff() } returns false
        every { user2.isNotAlarmTime() } returns false

        val users = mapOf(1L to user1, 2L to user2)

        val items = listOf(
            RegisteredItem(1, 11, ItemCondition(), "item1",TradeType.SELL, true,1L),
            RegisteredItem(2, 12, ItemCondition(), "item2", TradeType.SELL, true,1L),
            RegisteredItem(3, 13, ItemCondition(), "item3", TradeType.SELL, true,1L),
            RegisteredItem(4, 21, ItemCondition(), "item4", TradeType.SELL, true,2L),
            RegisteredItem(5, 22, ItemCondition(), "item5", TradeType.SELL, true,2L),
            RegisteredItem(6, 23, ItemCondition(), "item6", TradeType.SELL, true,2L)
        )

        // Common mock behaviors
        every { userService.getAllUserEntityMap() } returns users
        every { alertRepository.getRegisteredItem() } returns items
        every { alertRepository.getAllItemComments() } returns emptyList()
        every { alertRepository.syncBids(any(), any(), any()) } just runs
        every { discordService.sendDirectMessage(any(), any()) } just runs
    }
    @Test
    fun `Asynchronous Checker Performance Test`() = runBlocking {
        // Mock network call with non-blocking delay
        coEvery { malanClient.getItemBidList(any(), any()) } coAnswers { 
            delay(100)
            emptyList()
        }

        val time = measureTimeMillis {
            val job = asyncChecker.checkItem()
            job.join()
        }
        println("Asynchronous Checker took: $time ms")
        // Expected time: A bit more than 100ms, since all calls run concurrently.
    }
}
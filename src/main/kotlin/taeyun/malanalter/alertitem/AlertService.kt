package taeyun.malanalter.alertitem

import org.jetbrains.exposed.v1.core.StdOutSqlLogger
import org.jetbrains.exposed.v1.dao.with
import org.jetbrains.exposed.v1.jdbc.SizedIterable
import org.jetbrains.exposed.v1.jdbc.addLogger
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import taeyun.malanalter.alertitem.domain.AlertItemEntity
import taeyun.malanalter.alertitem.domain.AlertItemTable
import taeyun.malanalter.alertitem.domain.ItemBidEntity
import taeyun.malanalter.alertitem.dto.DiscordMessageContainer
import taeyun.malanalter.alertitem.dto.ItemBidDto
import taeyun.malanalter.alertitem.dto.ItemCondition
import taeyun.malanalter.alertitem.repository.AlertRepository
import taeyun.malanalter.auth.AlerterUserPrincipal
import taeyun.malanalter.auth.discord.DiscordService
import taeyun.malanalter.user.UserService

@Service
class AlertService(
    val discordService: DiscordService,
    val alertRepository: AlertRepository
) {
    fun sendTestDiscordMessage() {
        val loginUserId = UserService.getLoginUserId()
        discordService.sendDirectMessage(loginUserId, DiscordMessageContainer.testDiscordMessage())
    }

    fun saveNewAlertItem(itemId: Int, itemCondition: ItemCondition) {
        alertRepository.save(itemId, itemCondition)
        val loginUserId = UserService.getLoginUserId()
        discordService.sendDirectMessage(loginUserId, DiscordMessageContainer.alertItemRegisterMessage(itemId, itemCondition))
    }

    fun getAllBidOfUser(): Map<Int, List<ItemBidDto>> {
        val principal = SecurityContextHolder.getContext().authentication.principal as AlerterUserPrincipal
        return transaction {
//            addLogger(StdOutSqlLogger)
            // with 절을 하면 bid는 in 절에 한번에 가져오는데 이 후에 왜 다시 하나씩 alert_item을 조회?
            // 없애면 item 개수만큼 bid에서 가져온다.
            AlertItemEntity.find { AlertItemTable.userId eq principal.userId }
                .with(AlertItemEntity::bids)
                .associate { it.id.value to take5BidDto(it.bids) }
        }
    }

    private fun take5BidDto(bids: SizedIterable<ItemBidEntity>): List<ItemBidDto> =
        bids.filter { it.isAlarm }.take(5).map { ItemBidDto.from(it) }


}
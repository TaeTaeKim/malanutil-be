package taeyun.malanalter.alertitem

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.dao.with
import org.jetbrains.exposed.v1.jdbc.SizedIterable
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import taeyun.malanalter.alertitem.domain.AlertItemEntity
import taeyun.malanalter.alertitem.domain.AlertItemTable
import taeyun.malanalter.alertitem.domain.ItemBidEntity
import taeyun.malanalter.alertitem.dto.DiscordMessageContainer
import taeyun.malanalter.alertitem.dto.ItemBidDto
import taeyun.malanalter.alertitem.dto.ItemCondition
import taeyun.malanalter.alertitem.dto.TradeType
import taeyun.malanalter.alertitem.repository.AlertItemRepository
import taeyun.malanalter.alertitem.repository.AlertRepository
import taeyun.malanalter.auth.AlerterUserPrincipal
import taeyun.malanalter.auth.discord.DiscordService
import taeyun.malanalter.config.exception.AlerterBadRequest
import taeyun.malanalter.config.exception.ErrorCode
import taeyun.malanalter.user.UserService

private val logger = KotlinLogging.logger { }

@Service
class AlertService(
    val discordService: DiscordService,
    val alertRepository: AlertRepository
) {
    fun sendTestDiscordMessage() {
        val loginUserId = UserService.getLoginUserId()
        discordService.sendDirectMessage(loginUserId, DiscordMessageContainer.testDiscordMessage())
    }

    fun saveNewAlertItem(itemId: Int, itemCondition: ItemCondition, tradeType: TradeType?) {
        // todo : FE 반영전에는 무조건 SELL로 저장
        alertRepository.save(itemId, itemCondition, tradeType ?: TradeType.SELL)
        val loginUserId = UserService.getLoginUserId()
        discordService.sendDirectMessage(
            loginUserId,
            DiscordMessageContainer.alertItemRegisterMessage(itemId, itemCondition, tradeType?: TradeType.SELL)
        )

        logger.info { "유저 $loginUserId 아이템 등록 ${AlertItemRepository.getItemName(itemId)}" }
    }

    fun getAllBidOfUser(): Map<Int, List<ItemBidDto>> {
        val principal = SecurityContextHolder.getContext().authentication.principal as AlerterUserPrincipal
        return transaction {
//            addLogger(StdOutSqlLogger)
            // fixme : exposed N+1 처리의 이상함?
            // with 절을 하면 bid는 in 절에 한번에 가져오는데 이 후에 왜 다시 하나씩 alert_item을 조회?
            // 없애면 item 개수만큼 bid에서 가져온다.
            AlertItemEntity.find { AlertItemTable.userId eq principal.userId and (AlertItemTable.isAalarm eq true) }
                .with(AlertItemEntity::bids)
                .associate { it.id.value to take5BidDto(it.bids) }
        }
    }

    private fun take5BidDto(bids: SizedIterable<ItemBidEntity>): List<ItemBidDto> {
        val filteredBids = bids.filter { it.isAlarm }
        if (filteredBids.isEmpty()) return emptyList()
        
        val tradeType = filteredBids.first().alertItem.tradeType
        return filteredBids
            .sortedWith(
                if (tradeType == TradeType.BUY) {
                    compareByDescending { it.price }
                } else {
                    compareBy { it.price }
                }
            )
            .take(5)
            .map { ItemBidDto.from(it) }
    }

    fun turnOffBid(bidId: Long) {
        transaction {
            ItemBidEntity.findByIdAndUpdate(bidId) {
                if (it.alertItem.userId == UserService.getLoginUserId()) {
                    it.isAlarm = false
                } else {
                    throw AlerterBadRequest(ErrorCode.UNAUTHORIZED, "인가되지 않은 bid에 대한 삭제요청", true)
                }
            }
        }
    }
}
package taeyun.malanalter.alertitem

import org.springframework.stereotype.Service
import taeyun.malanalter.alertitem.dto.DiscordMessage
import taeyun.malanalter.alertitem.dto.ItemCondition
import taeyun.malanalter.alertitem.repository.AlertRepository
import taeyun.malanalter.auth.discord.DiscordService
import taeyun.malanalter.user.UserService

@Service
class AlertService(
    val discordService: DiscordService,
    val alertRepository: AlertRepository
) {
    fun sendTestDiscordMessage() {
        val loginUserId = UserService.getLoginUserId()
        discordService.sendDirectMessage(loginUserId, DiscordMessage.testDiscordMessage())
    }

    fun saveNewAlertItem(itemId: Int, itemCondition: ItemCondition) {
        alertRepository.save(itemId, itemCondition)
        val loginUserId = UserService.getLoginUserId()
        discordService.sendDirectMessage(loginUserId, DiscordMessage.alertItemRegisterMessage(itemId, itemCondition))


    }
}
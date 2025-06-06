package taeyun.malanalter.alertitem

import org.springframework.stereotype.Service
import taeyun.malanalter.alertitem.dto.DiscordMessage
import taeyun.malanalter.auth.discord.DiscordService
import taeyun.malanalter.user.UserService

@Service
class AlertService(val discordService: DiscordService) {
    fun sendTestDiscordMessage() {
        val loginUserId = UserService.getLoginUserId()
        discordService.sendDirectMessage(loginUserId, DiscordMessage.testDiscordMessage())
    }
}
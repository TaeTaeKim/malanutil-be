package taeyun.malanalter.alertitem

import feign.Feign
import org.springframework.stereotype.Service
import taeyun.malanalter.alertitem.dto.DiscordMessage
import taeyun.malanalter.feignclient.DiscordClient

@Service
class AlertService(val feignBuilder: Feign.Builder) {
    fun sendTestDiscordMessage(webhookUrl: String) {
        val client = feignBuilder.target(DiscordClient::class.java, webhookUrl)
        client.sendDiscordMessage(DiscordMessage.testDiscordMessage())

    }
}
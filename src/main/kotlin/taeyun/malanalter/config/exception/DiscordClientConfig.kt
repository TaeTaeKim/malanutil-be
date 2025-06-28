package taeyun.malanalter.config.exception

import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import taeyun.malanalter.config.property.DiscordProperties

@Component
class DiscordClientConfig(val discordProperties: DiscordProperties) {

    @Bean
    fun discordClient(): WebClient {
        return WebClient.create(discordProperties.webhookUrl)
    }
}